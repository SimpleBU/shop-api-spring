package com.example.shop.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record Shipment(
        String id,
        String orderId,
        String carrier,
        String trackingNumber,
        ShipmentStatus status,
        Address destination,
        LocalDate estimatedDelivery,
        List<ShipmentDocument> documents,
        OffsetDateTime createdAt) {

    public Shipment withDocuments(List<ShipmentDocument> newDocuments) {
        return new Shipment(id, orderId, carrier, trackingNumber, status, destination, estimatedDelivery,
                newDocuments, createdAt);
    }
}
