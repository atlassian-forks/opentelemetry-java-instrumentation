/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webflux.v5_3.server;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.reactor.ContextPropagationOperator;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

public final class SpringWebfluxTelemetry {
  private final Instrumenter<ServerHttpRequest, ServerHttpResponse> instrumenter;

  public static SpringWebfluxTelemetry create(OpenTelemetry openTelemetry) {
    return builder(openTelemetry).build();
  }

  public static SpringWebfluxTelemetryBuilder builder(OpenTelemetry openTelemetry) {
    return new SpringWebfluxTelemetryBuilder(openTelemetry);
  }

  public static void registerReactorHook() {
    ContextPropagationOperator.builder().build().registerOnEachOperator();
  }

  SpringWebfluxTelemetry(Instrumenter<ServerHttpRequest, ServerHttpResponse> instrumenter) {
    this.instrumenter = instrumenter;
  }

  public TelemetryProducingWebFilter createWebFilter() {
    return new TelemetryProducingWebFilter(instrumenter);
  }
}
