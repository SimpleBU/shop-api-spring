package com.example.shop.config;

import com.example.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(CatalogRefreshJob.class);

    private final ProductService productService;

    public CatalogRefreshJob(ProductService productService) {
        this.productService = productService;
    }

    @Scheduled(cron = "${shop.catalog.refresh-cron}")
    public void refreshCatalogSnapshot() {
        int size = productService.refreshSnapshot();
        log.debug("catalog snapshot refreshed, {} products", size);
    }

    @Scheduled(fixedDelay = 300_000L)
    public void expireReservedStock() {
        int released = productService.releaseExpiredReservations();
        if (released > 0) {
            log.debug("released {} expired stock reservations", released);
        }
    }
}
