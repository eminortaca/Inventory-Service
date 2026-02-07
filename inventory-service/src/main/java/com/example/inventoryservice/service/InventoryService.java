package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        // 1. Veritabanından listedeki kodlara uyanları bul
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                // 2. Bulunan her ürünü (Inventory) -> Cevap Kutusuna (InventoryResponse) dönüştür
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList(); // 3. Hepsini liste yapıp geri döndür
    }
}