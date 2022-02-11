/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class LogAssertionsTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("instrumentation_library", null);
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID = "0000000000000003";
  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put("bear", "mya")
          .put("warm", true)
          .put("temperature", 30)
          .put("length", 1.2)
          .put("colors", "red", "blue")
          .put("conditions", false, true)
          .put("scores", 0L, 1L)
          .put("coins", 0.01, 0.05, 0.1)
          .build();

  @SuppressWarnings("deprecation") // test deprecated setName method
  private static final LogData LOG_DATA =
      LogDataBuilder.create(RESOURCE, INSTRUMENTATION_LIBRARY_INFO)
          .setEpoch(100, TimeUnit.NANOSECONDS)
          .setSpanContext(
              SpanContext.create(
                  TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
          .setSeverity(Severity.INFO)
          .setSeverityText("info")
          .setName("name")
          .setBody("message")
          .setAttributes(ATTRIBUTES)
          .build();

  @Test
  @SuppressWarnings("deprecation") // test deprecated hasName method
  void passing() {
    assertThat(LOG_DATA)
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
        .hasEpochNanos(100)
        .hasSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .hasSeverity(Severity.INFO)
        .hasSeverityText("info")
        .hasName("name")
        .hasBody("message")
        .hasAttributes(ATTRIBUTES)
        .hasAttributes(
            attributeEntry("bear", "mya"),
            attributeEntry("warm", true),
            attributeEntry("temperature", 30),
            attributeEntry("length", 1.2),
            attributeEntry("colors", "red", "blue"),
            attributeEntry("conditions", false, true),
            attributeEntry("scores", 0L, 1L),
            attributeEntry("coins", 0.01, 0.05, 0.1))
        .hasAttributesSatisfying(
            attributes ->
                OpenTelemetryAssertions.assertThat(attributes)
                    .hasSize(8)
                    .containsEntry(AttributeKey.stringKey("bear"), "mya")
                    .hasEntrySatisfying(
                        AttributeKey.stringKey("bear"), value -> assertThat(value).hasSize(3))
                    .containsEntry("bear", "mya")
                    .containsEntry("warm", true)
                    .containsEntry("temperature", 30)
                    .containsEntry(AttributeKey.longKey("temperature"), 30L)
                    .containsEntry(AttributeKey.longKey("temperature"), 30)
                    .containsEntry("length", 1.2)
                    .containsEntry("colors", "red", "blue")
                    .containsEntryWithStringValuesOf("colors", Arrays.asList("red", "blue"))
                    .containsEntry("conditions", false, true)
                    .containsEntryWithBooleanValuesOf("conditions", Arrays.asList(false, true))
                    .containsEntry("scores", 0L, 1L)
                    .containsEntryWithLongValuesOf("scores", Arrays.asList(0L, 1L))
                    .containsEntry("coins", 0.01, 0.05, 0.1)
                    .containsEntryWithDoubleValuesOf("coins", Arrays.asList(0.01, 0.05, 0.1))
                    .containsKey(AttributeKey.stringKey("bear"))
                    .containsKey("bear")
                    .containsOnly(
                        attributeEntry("bear", "mya"),
                        attributeEntry("warm", true),
                        attributeEntry("temperature", 30),
                        attributeEntry("length", 1.2),
                        attributeEntry("colors", "red", "blue"),
                        attributeEntry("conditions", false, true),
                        attributeEntry("scores", 0L, 1L),
                        attributeEntry("coins", 0.01, 0.05, 0.1)));
  }

  @Test
  @SuppressWarnings("deprecation") // test deprecated hasName method
  void failure() {
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasResource(Resource.empty()));
    assertThatThrownBy(
        () -> assertThat(LOG_DATA).hasInstrumentationLibrary(InstrumentationLibraryInfo.empty()));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasEpochNanos(200));
    assertThatThrownBy(
        () ->
            assertThat(LOG_DATA)
                .hasSpanContext(
                    SpanContext.create(
                        TRACE_ID,
                        "0000000000000004",
                        TraceFlags.getDefault(),
                        TraceState.getDefault())));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasSeverity(Severity.DEBUG));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasSeverityText("warning"));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasName("foo"));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasBody("bar"));
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasAttributes(Attributes.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(LOG_DATA).hasAttributes(attributeEntry("food", "burger")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .containsEntry("cat", "bark")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .containsKey(AttributeKey.stringKey("cat"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes).containsKey("cat")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes -> OpenTelemetryAssertions.assertThat(attributes).isEmpty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes -> OpenTelemetryAssertions.assertThat(attributes).hasSize(33)))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LOG_DATA)
                    .hasAttributesSatisfying(
                        attributes ->
                            OpenTelemetryAssertions.assertThat(attributes)
                                .hasEntrySatisfying(
                                    AttributeKey.stringKey("bear"),
                                    value -> assertThat(value).hasSize(2))))
        .isInstanceOf(AssertionError.class);
  }
}
