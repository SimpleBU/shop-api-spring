package com.example.shop.model;

import java.time.Instant;

public record AuditEvent(String actor, String action, String target, Instant at) {

    public static AuditEvent now(String actor, String action, String target) {
        return new AuditEvent(actor, action, target, Instant.now());
    }
}
