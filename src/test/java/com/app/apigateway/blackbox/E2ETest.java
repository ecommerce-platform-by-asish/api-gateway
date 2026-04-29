package com.app.apigateway.blackbox;

import com.app.apigateway.stubs.AuthStubs;
import com.app.apigateway.stubs.ProductStubs;
import com.app.common.annotation.BlackboxTest;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@BlackboxTest
@DisplayName("Security Blackbox Tests (Filter Validation)")
class E2ETest {

  @Container
  @ServiceConnection(name = "redis")
  private static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"));

  static {
    redis.addExposedPort(6379);
  }

  private static final AuthStubs authStubs = new AuthStubs();
  private static final ProductStubs productStubs = authStubs.forProducts();

  @LocalServerPort private int port;

  private WebTestClient webTestClient;
  private KeyPair testKeyPair;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("wiremock.url", authStubs::getBaseUrl);
  }

  @BeforeEach
  void setUp() throws Exception {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

    // Generate a fresh key pair for each test
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    testKeyPair = kpg.generateKeyPair();

    authStubs.resetAll();
    authStubs.stubJwks((RSAPublicKey) testKeyPair.getPublic());
  }

  private String createTestToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .claim("id", "test-user-id")
        .claim("role", "ADMIN")
        .header()
        .keyId("test-key-id")
        .and()
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 3600000))
        .signWith(testKeyPair.getPrivate())
        .compact();
  }

  @Test
  @DisplayName("Blackbox: Should enforce rate limiting on the Gateway")
  void validateRateLimiting() {
    authStubs.stubLogin("test-token");

    // Send enough requests to overwhelm the 10-token bucket even with replenishment
    for (int i = 0; i < 30; i++) {
      webTestClient
          .post()
          .uri("/api/auth/login")
          .bodyValue(Map.of("username", "admin", "password", "password"))
          .exchange();
    }

    webTestClient
        .post()
        .uri("/api/auth/login")
        .bodyValue(Map.of("username", "admin", "password", "password"))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  @DisplayName("Blackbox: Should successfully route login requests to Auth Service")
  void validateLoginRouting() {
    String mockToken = "mock-e2e-token";
    authStubs.stubLogin(mockToken);

    webTestClient
        .post()
        .uri("/api/auth/login")
        .bodyValue(Map.of("username", "admin", "password", "password"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessToken")
        .isEqualTo(mockToken);
  }

  @Test
  @DisplayName("Blackbox: Should successfully route product requests with valid JWT")
  void validateProductRoutingWithToken() {
    productStubs.stubProductService();
    String token = createTestToken("admin-user");

    webTestClient
        .get()
        .uri("/api/products/test")
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("success");
  }

  @Test
  @DisplayName("Blackbox: Should block product requests without token")
  void validateProductBlockingWithoutToken() {
    productStubs.stubProductService();

    webTestClient
        .get()
        .uri("/api/products/test")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
