/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webflux.v5_0.server;

import io.opentelemetry.instrumentation.testing.junit.http.AbstractHttpServerTest;
import io.opentelemetry.instrumentation.testing.junit.http.HttpServerTestOptions;
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint;
import org.springframework.context.ConfigurableApplicationContext;

public final class SpringWebfluxServerInstrumentationTest
    extends AbstractHttpServerTest<ConfigurableApplicationContext> {

  private static final String CONTEXT_PATH = "/test";

  @Override
  protected ConfigurableApplicationContext setupServer() {
    return TestWebfluxSpringBootApp.start(port, CONTEXT_PATH);
  }

  @Override
  public void stopServer(ConfigurableApplicationContext applicationContext) {
    applicationContext.close();
  }

  @Override
  protected void configure(HttpServerTestOptions options) {
    options.setContextPath(CONTEXT_PATH);
    options.setTestPathParam(true);
    // servlet filters don't capture exceptions thrown in controllers
    // options.setTestException(false);

    options.setExpectedHttpRoute(
        endpoint -> {
          if (endpoint == ServerEndpoint.PATH_PARAM) {
            return CONTEXT_PATH + "/path/{id}/param";
          }
          return expectedHttpRoute(endpoint);
        });
  }
}
