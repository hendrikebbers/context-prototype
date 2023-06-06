package com.swirlds.base.sample;

import com.swirlds.base.context.ThreadContext;
import com.swirlds.base.logging.Logger;
import com.swirlds.base.span.Span;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.ValueDescriptor;

public class Sample3 {

    private final static Logger logger = Logger.getLogger(Sample1.class);

    public static Span createStartEvent(String eventType, String label, String description, String value) {
        List<AnnotationElement> typeAnnotations = new ArrayList<>();
        typeAnnotations.add(new AnnotationElement(Name.class, "com.swirlds.events.generated." + eventType + "Event"));
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

        EventFactory f = EventFactory.create(typeAnnotations, fields);
        Event event = f.newEvent();
        String uuid = UUID.randomUUID().toString();
        return new Span() {
            @Override
            public String getData() {
                return value;
            }

            @Override
            public String getSpanId() {
                return uuid;
            }

            @Override
            public String getParentSpanId() {
                return null;
            }

            @Override
            public String getType() {
                return eventType;
            }

            public void start() {
                ThreadContext.addMetadata(getType(), getData());
                event.begin();
                event.set(0, getType());
                event.set(1, getSpanId());
                event.set(3, value);
            }

            @Override
            public void commit() {
                end(true);
                ThreadContext.removeMetadata(getType());
            }

            @Override
            public void fail() {
                end(false);
            }

            private void end(final boolean success) {
                event.set(4, success);
                event.commit();
            }
        };
    }

    public static void main(String[] args) throws Exception {
        Span span = createStartEvent("SmartContractCall", "SmartContractCallStartEvent",
                "Event that is triggered when a smart contract is called", "hello_world.sol");
        logger.info("Hello World");
        Thread.sleep(1000);
        span.fail();
    }
}
