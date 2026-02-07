package com.example.inventoryservice;

import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    // UYGULAMA BAŞLARKEN ÇALIŞACAK OLAN METOD
    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            // 1. Ürün: Stokta VAR (100 tane)
            Inventory inventory1 = new Inventory();
            inventory1.setSkuCode("iphone_13");
            inventory1.setQuantity(100);

            // 2. Ürün: Stokta YOK (0 tane)
            Inventory inventory2 = new Inventory();
            inventory2.setSkuCode("iphone_13_red");
            inventory2.setQuantity(0);

            // Bunları veritabanına kaydet
            inventoryRepository.save(inventory1);
            inventoryRepository.save(inventory2);

            System.out.println("----------------------------------------");
            System.out.println("Örnek veriler veritabanına yüklendi!");
            System.out.println("----------------------------------------");
        };
    }
}