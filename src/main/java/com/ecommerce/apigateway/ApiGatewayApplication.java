package com.ecommerce.apigateway;

import com.common.boot.BaseSpringBootApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import reactor.core.publisher.Hooks;

@BaseSpringBootApplication
public class ApiGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }

  @PostConstruct
  public void init() {
    Hooks.enableAutomaticContextPropagation();
  }
}
