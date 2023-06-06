package com.swirlds.base.sample;

import java.time.Duration;
import java.util.concurrent.Executors;
import jdk.jfr.consumer.RecordingStream;

public class Sampl2 {

    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        System.out.println("1");
        try (var rs = new RecordingStream()) {
            rs.enable("jdk.CPULoad").withPeriod(Duration.ofSeconds(1));
            rs.onEvent(a -> System.out.println(a));
            rs.start();
        }
    }
}
