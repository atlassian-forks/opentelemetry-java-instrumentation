/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webflux.v5_0.client;

import static io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor.alwaysClient;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesExtractorBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import io.opentelemetry.instrumentation.spring.webflux.v5_0.client.internal.SpringWebClientNetAttributesGetter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

/** A builder of {@link SpringWebClientTelemetry}. */
public final class SpringWebClientTelemetryBuilder {

  private final OpenTelemetry openTelemetry;
  private final List<AttributesExtractor<ClientRequest, ClientResponse>> additionalExtractors =
      new ArrayList<>();
  private final HttpClientAttributesExtractorBuilder<ClientRequest, ClientResponse>
      httpAttributesExtractorBuilder =
      HttpClientAttributesExtractor.builder(
          SpringWebClientHttpAttributesGetter.INSTANCE, new SpringWebClientNetAttributesGetter());

  private boolean captureExperimentalSpanAttributes = false;

  SpringWebClientTelemetryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /**
   * Adds an additional {@link AttributesExtractor} to invoke to set attributes to instrumented
   * items.
   */
  @CanIgnoreReturnValue
  public SpringWebClientTelemetryBuilder addAttributesExtractor(
      AttributesExtractor<ClientRequest, ClientResponse> attributesExtractor) {
    additionalExtractors.add(attributesExtractor);
    return this;
  }

  /**
   * Configures the HTTP request headers that will be captured as span attributes.
   *
   * @param requestHeaders A list of HTTP header names.
   */
  @CanIgnoreReturnValue
  public SpringWebClientTelemetryBuilder setCapturedRequestHeaders(List<String> requestHeaders) {
    httpAttributesExtractorBuilder.setCapturedRequestHeaders(requestHeaders);
    return this;
  }

  /**
   * Configures the HTTP response headers that will be captured as span attributes.
   *
   * @param responseHeaders A list of HTTP header names.
   */
  @CanIgnoreReturnValue
  public SpringWebClientTelemetryBuilder setCapturedResponseHeaders(List<String> responseHeaders) {
    httpAttributesExtractorBuilder.setCapturedResponseHeaders(responseHeaders);
    return this;
  }

  /**
   * Sets whether experimental attributes should be set to spans. These attributes may be changed or
   * removed in the future, so only enable this if you know you do not require attributes filled by
   * this instrumentation to be stable across versions.
   */
  @CanIgnoreReturnValue
  public SpringWebClientTelemetryBuilder setCaptureExperimentalSpanAttributes(
      boolean captureExperimentalSpanAttributes) {
    this.captureExperimentalSpanAttributes = captureExperimentalSpanAttributes;
    return this;
  }

  /**
   * Returns a new {@link SpringWebClientTelemetry} with the settings of this {@link
   * SpringWebClientTelemetryBuilder}.
   */
  public SpringWebClientTelemetry build() {
    SpringWebClientHttpAttributesGetter httpAttributesGetter =
        SpringWebClientHttpAttributesGetter.INSTANCE;

    InstrumenterBuilder<ClientRequest, ClientResponse> builder =
        Instrumenter.<ClientRequest, ClientResponse>builder(
                openTelemetry,
                "io.opentelemetry.spring-webflux-5.0",
                HttpSpanNameExtractor.create(httpAttributesGetter))
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(httpAttributesGetter))
            .addAttributesExtractor(httpAttributesExtractorBuilder.build())
            .addAttributesExtractors(additionalExtractors)
            .addOperationMetrics(HttpClientMetrics.get());

    if (captureExperimentalSpanAttributes) {
      builder.addAttributesExtractor(new SpringWebClientExperimentalAttributesExtractor());
    }

    // headers are injected elsewhere; ClientRequest is immutable
    Instrumenter<ClientRequest, ClientResponse> instrumenter =
        builder.buildInstrumenter(alwaysClient());

    return new SpringWebClientTelemetry(instrumenter, openTelemetry.getPropagators());
  }
}
