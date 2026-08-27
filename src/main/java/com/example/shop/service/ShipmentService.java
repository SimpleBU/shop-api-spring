package com.example.shop.service;

import com.example.shop.dto.Address;
import com.example.shop.dto.Shipment;
import com.example.shop.dto.ShipmentCreateRequest;
import com.example.shop.dto.ShipmentDocument;
import com.example.shop.dto.ShipmentStatus;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShipmentService {

    private final Map<String, Shipment> shipments = new ConcurrentHashMap<>();
    private final SequenceGenerator ids = new SequenceGenerator("shp", 300);

    public ShipmentService() {
        Address moscow = new Address("RU", "Moscow", "Tverskaya 1", "125009");
        String id = ids.next();
        shipments.put(id, new Shipment(id, "ord-5001", "CDEK", "CD00099122", ShipmentStatus.IN_TRANSIT,
                moscow, LocalDate.now().plusDays(2), List.of(), OffsetDateTime.now()));
        String second = ids.next();
        shipments.put(second, new Shipment(second, "ord-5002", "POCHTA", "RU8812340", ShipmentStatus.CREATED,
                moscow, LocalDate.now().plusDays(6), List.of(), OffsetDateTime.now()));
    }

    public List<Shipment> findAll(ShipmentStatus status, String carrier) {
        return shipments.values().stream()
                .filter(s -> status == null || s.status() == status)
                .filter(s -> carrier == null || s.carrier().equalsIgnoreCase(carrier))
                .sorted(Comparator.comparing(Shipment::id))
                .toList();
    }

    public Shipment findById(String id) {
        Shipment shipment = shipments.get(id);
        if (shipment == null) {
            throw new ResourceNotFoundException("Shipment", id);
        }
        return shipment;
    }

    public Shipment create(ShipmentCreateRequest request) {
        String id = ids.next();
        Shipment shipment = new Shipment(id, request.orderId(), request.carrier(),
                request.carrier().substring(0, 2).toUpperCase() + System.nanoTime() % 100000000L,
                ShipmentStatus.CREATED, request.destination(), request.estimatedDelivery(), List.of(),
                OffsetDateTime.now());
        shipments.put(id, shipment);
        return shipment;
    }

    public ShipmentDocument attachDocument(String id, String kind, String fileName, long sizeBytes) {
        Shipment shipment = findById(id);
        ShipmentDocument document = new ShipmentDocument("doc-" + (shipment.documents().size() + 1) + "-" + id,
                kind, fileName, sizeBytes, OffsetDateTime.now());
        List<ShipmentDocument> documents = new ArrayList<>(shipment.documents());
        documents.add(document);
        shipments.put(id, shipment.withDocuments(List.copyOf(documents)));
        return document;
    }

    public byte[] renderInvoicePdf(String number) {
        String body = "%PDF-1.4\n% shipment invoice " + number + " generated for the shop test bench\n%%EOF\n";
        return body.getBytes(StandardCharsets.ISO_8859_1);
    }

    public int size() {
        return shipments.size();
    }
}
