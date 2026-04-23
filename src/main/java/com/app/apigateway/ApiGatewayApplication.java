package com.app.apigateway;

import com.app.common.boot.BaseSpringBootApplication;
import org.springframework.boot.SpringApplication;

@BaseSpringBootApplication(enableActuator = true, enableOpenApi = true)
public class ApiGatewayApplication {

  static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
