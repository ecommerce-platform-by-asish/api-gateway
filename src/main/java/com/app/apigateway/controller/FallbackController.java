package com.app.apigateway.controller;

import com.app.common.exception.ServiceUnavailableException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Generic fallback controller for all microservices. Handles circuit breaker triggers by throwing a
 * standardized service unavailable exception.
 */
@RestController
public class FallbackController {

  @RequestMapping("/fallback")
  public Mono<Void> fallback() {
    return Mono.error(new ServiceUnavailableException());
  }
}
