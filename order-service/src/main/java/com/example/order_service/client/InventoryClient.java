package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "inventory-service", url = "http://inventory-service:8080")
public interface InventoryClient {
    @GetMapping("/hello")
    String getHelloFromInventory();
}