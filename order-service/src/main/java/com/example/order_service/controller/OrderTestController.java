package com.example.order_service.controller;

import com.example.order_service.client.InventoryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-test")
public class OrderTestController {

    private final InventoryClient inventoryClient;

    // Dependency Injection (Bağımlılık Enjeksiyonu)
    public OrderTestController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @GetMapping("/hello-from-inventory")
    public String checkInventory() {
        // Burada Order servisi, Inventory servisine gidip veriyi alıyor
        String message = inventoryClient.getHelloFromInventory();
        return "Order Servisi Diyor ki: " + message;
    }
}