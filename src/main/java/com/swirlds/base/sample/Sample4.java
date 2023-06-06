package com.swirlds.base.sample;

import com.swirlds.base.context.ThreadContext;
import com.swirlds.base.logging.Logger;
import com.swirlds.base.resources.ResourceManager;
import com.swirlds.base.span.Span;
import com.swirlds.base.span.SpanDefinition;
import com.swirlds.base.span.SpanFactory;

public class Sample4 {

    private final static Logger logger = Logger.getLogger(Sample1.class);

    public static void main(String[] args) throws Exception {
        ThreadContext.addMetadata("foo", "test");
        SpanFactory spanFactory = SpanFactory.createSpanFactory(new SpanDefinition("TransactionCall"));
        Span span = spanFactory.create("0x28bd823");
        try {
            logger.info("Hello World");
            Thread.sleep(500);

            ResourceManager.getInstance().getExecutor().execute(() -> {
                logger.info("Calling Smart Contract");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, new SpanDefinition("ContractCall"), "contract.sol", span);
            Thread.sleep(2_000);
        } finally {
            span.fail();
        }
    }
}
