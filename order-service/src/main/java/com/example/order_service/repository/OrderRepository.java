package com.example.order_service.repository;

import com.example.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Şimdilik buraya ekstra bir kod yazmamıza gerek yok.
    // JpaRepository sayesinde save, findAll, findById gibi metodlar zaten hazır geldi.
}