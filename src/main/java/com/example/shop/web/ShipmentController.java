package com.example.shop.web;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.Shipment;
import com.example.shop.dto.ShipmentCreateRequest;
import com.example.shop.dto.ShipmentDocument;
import com.example.shop.dto.ShipmentStatus;
import com.example.shop.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(ShipmentController.BASE)
public class ShipmentController {

    public static final String BASE = "/api/v1/shipments";

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Shipment>> list(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) String carrier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Shipment> found = shipmentService.findAll(status, carrier);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getOne(@PathVariable String id) {
        return ResponseEntity.ok(shipmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Shipment> create(@Valid @RequestBody ShipmentCreateRequest request) {
        Shipment shipment = shipmentService.create(request);
        return ResponseEntity.created(URI.create(BASE + "/" + shipment.id())).body(shipment);
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShipmentDocument> uploadDocument(@PathVariable String id,
                                                           @RequestParam("kind") String kind,
                                                           @RequestPart("file") MultipartFile file) {
        ShipmentDocument document = shipmentService.attachDocument(id, kind,
                file.getOriginalFilename() == null ? "document.bin" : file.getOriginalFilename(),
                file.getSize());
        return ResponseEntity.created(URI.create(BASE + "/" + id + "/documents/" + document.id()))
                .body(document);
    }

    @GetMapping(value = "/invoices/{number:[0-9]{6}}", produces = "application/pdf")
    public ResponseEntity<byte[]> invoice(@PathVariable String number) {
        byte[] pdf = shipmentService.renderInvoicePdf(number);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-" + number + ".pdf\"")
                .body(pdf);
    }
}
