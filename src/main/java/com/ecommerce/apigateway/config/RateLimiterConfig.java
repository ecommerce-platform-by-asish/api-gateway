package com.ecommerce.apigateway.config;

import java.util.Objects;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

  /**
   * Rate limiting by client IP address. Effectively prevents brute-force login attempts from a
   * single source.
   */
  @Bean
  @Primary
  public KeyResolver userKeyResolver() {
    return exchange ->
        Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress()
                .getHostAddress());
  }
}
