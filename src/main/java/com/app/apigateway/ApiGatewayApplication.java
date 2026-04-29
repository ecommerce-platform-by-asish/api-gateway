package com.app.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    excludeName = {
      "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
      "org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration",
      "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration"
    })
public class ApiGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
