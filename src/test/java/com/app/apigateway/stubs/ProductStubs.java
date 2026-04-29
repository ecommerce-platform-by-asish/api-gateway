package com.app.apigateway.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Stubs for the Product Service. Shares the WireMock server started by {@link AuthStubs} — obtain
 * an instance via {@code authStubs.forProducts()} rather than instantiating directly.
 */
public class ProductStubs {

  private final WireMock client;

  ProductStubs(WireMock client) {
    this.client = client;
  }

  /** Stubs all product endpoints to return a successful empty list response. */
  public void stubProductService() {
    client.register(
        get(urlPathMatching("/api/products/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody("{\"status\":\"success\",\"data\":[]}")));
  }
}
