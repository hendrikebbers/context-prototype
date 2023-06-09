package com.swirlds.base.span.impl;

import com.swirlds.base.context.DiagnosticContext;
import com.swirlds.base.span.Span;
import com.swirlds.base.span.SpanDefinition;
import com.swirlds.base.span.SpanFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.ValueDescriptor;

public class SpanFactoryProviderImpl {

    public static SpanFactory createSpanFactory(SpanDefinition spanDefinition) {
        DiagnosticContext diagnosticContext = DiagnosticContext.getCurrent();

        List<AnnotationElement> typeAnnotations = new ArrayList<>();
        typeAnnotations.add(new AnnotationElement(Name.class,
                "com.swirlds.events.generated." + spanDefinition.eventType() + "Event"));
        if (spanDefinition.label() != null) {
            typeAnnotations.add(new AnnotationElement(Label.class, spanDefinition.label()));
        }
        if (spanDefinition.description() != null) {
            typeAnnotations.add(new AnnotationElement(Description.class, spanDefinition.description()));
        }
        typeAnnotations.add(new AnnotationElement(Category.class, new String[]{"SwirldsLabs"}));

        List<ValueDescriptor> fields = new ArrayList<>();
        fields.add(new ValueDescriptor(String.class, "type", List.of(new AnnotationElement(Label.class, "type"))));
        fields.add(new ValueDescriptor(String.class, "spanId", List.of(new AnnotationElement(Label.class, "spanId"))));
        fields.add(
                new ValueDescriptor(String.class, "parentSpanId",
                        List.of(new AnnotationElement(Label.class, "parentSpanId"))));
        fields.add(new ValueDescriptor(String.class, "data",
                List.of(new AnnotationElement(Label.class, "data"))));
        fields.add(new ValueDescriptor(Boolean.TYPE, "success",
                List.of(new AnnotationElement(Label.class, "success"))));

        EventFactory eventFactory = EventFactory.create(typeAnnotations, fields);
        return new SpanFactory() {
            @Override
            public Span create(final Span parentSpan, final String data) {
                var event = eventFactory.newEvent();
                var uuid = UUID.randomUUID().toString();
                if (data != null) {
                    diagnosticContext.addMetadata(spanDefinition.eventType(), data);
                    diagnosticContext.addMetadata("spanId", uuid);
                }
                event.begin();
                event.set(0, spanDefinition.eventType());
                event.set(1, uuid);
                if (parentSpan != null) {
                    event.set(2, parentSpan.getSpanId());
                }
                event.set(3, data);

                return new Span() {
                    @Override
                    public String getData() {
                        return data;
                    }

                    @Override
                    public String getSpanId() {
                        return uuid;
                    }

                    @Override
                    public String getParentSpanId() {
                        if (parentSpan != null) {
                            return parentSpan.getSpanId();
                        }
                        return null;
                    }

                    @Override
                    public String getType() {
                        return spanDefinition.eventType();
                    }

                    @Override
                    public void commit() {
                        end(true);
                        diagnosticContext.removeMetadata(getType());
                    }

                    @Override
                    public void fail() {
                        end(false);
                    }

                    private void end(final boolean success) {
                        event.set(4, success);
                        event.commit();
                        diagnosticContext.removeMetadata("spanId");
                    }
                };
            }
        };
    }

}
