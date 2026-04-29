package com.app.apigateway;

import com.app.common.boot.BaseSpringBootApplication;
import org.springframework.boot.SpringApplication;

@BaseSpringBootApplication(
    excludeName = {
      "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
      "org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration",
      "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration"
    },
    enableActuator = true)
public class ApiGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
