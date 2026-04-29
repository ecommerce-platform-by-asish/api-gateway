package com.app.apigateway.stubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base infrastructure for WireMock stub servers backed by a Testcontainers-managed WireMock
 * container. Uses an instance-scoped {@link WireMock} client to avoid global static state.
 */
public abstract class StubServer implements AutoCloseable {

  private final WireMockContainer container;
  protected final WireMock client;

  /** Specialized container class for WireMock. */
  static class WireMockContainer extends GenericContainer<WireMockContainer> {
    WireMockContainer() {
      super(DockerImageName.parse("wiremock/wiremock:3.11.0"));
      withExposedPorts(8080);
    }
  }

  protected StubServer() {
    this.container = new WireMockContainer();
    this.container.start();
    this.client = new WireMock(container.getHost(), container.getMappedPort(8080));
  }

  /** Returns the base URL of the running WireMock server. */
  public String getBaseUrl() {
    if (!container.isRunning()) {
      throw new IllegalStateException("Stub server is not running");
    }
    return "http://" + container.getHost() + ":" + container.getMappedPort(8080);
  }

  /** Resets all stub mappings on the running WireMock server. */
  public void resetAll() {
    client.resetMappings();
  }

  @Override
  public void close() {
    if (container.isRunning()) {
      container.stop();
    }
  }
}
