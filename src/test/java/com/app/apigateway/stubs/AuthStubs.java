package com.app.apigateway.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/** Specialized stub server for simulating the Auth Service and its JWKS endpoint. */
public class AuthStubs extends StubServer {

  /** Stubs a standard login response returning a mock access token. */
  public void stubLogin(String token) {
    client.register(
        post(urlEqualTo("/api/auth/login"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(String.format("{\"accessToken\":\"%s\"}", token))));
  }

  /**
   * Stubs the JWKS endpoint with the provided RSA public key, enabling real JWT signature
   * verification in tests.
   */
  public void stubJwks(RSAPublicKey publicKey) {
    String n =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(publicKey.getModulus().toByteArray());
    String e =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(publicKey.getPublicExponent().toByteArray());

    String jwks =
        String.format(
            "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"test-key-id\",\"n\":\"%s\",\"e\":\"%s\"}]}",
            n, e);

    client.register(
        get(urlEqualTo("/api/auth/.well-known/jwks.json"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jwks)));
  }

  /** Stubs the JWKS endpoint to return an empty key set (no valid keys). */
  public void stubEmptyJwks() {
    client.register(
        get(urlEqualTo("/api/auth/.well-known/jwks.json"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody("{\"keys\":[]}")));
  }

  /**
   * Returns a {@link ProductStubs} that targets this same WireMock server. This avoids starting a
   * second container just for product endpoints.
   */
  public ProductStubs forProducts() {
    return new ProductStubs(client);
  }
}
