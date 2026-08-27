package com.example.shop.model;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceGenerator {

    private final String prefix;
    private final AtomicLong counter;

    public SequenceGenerator(String prefix, long start) {
        this.prefix = prefix;
        this.counter = new AtomicLong(start);
    }

    public String next() {
        return prefix + "-" + counter.incrementAndGet();
    }
}
