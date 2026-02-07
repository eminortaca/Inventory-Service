package com.example.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // 1. Bu sınıfın bir Veritabanı Tablosu olduğunu söyler.
@Table(name = "t_inventory") // 2. Tablonun adı 't_inventory' olsun.
@Getter
@Setter
@AllArgsConstructor // 3. Tüm özellikleri içeren yapıcı metod (Constructor).
@NoArgsConstructor  // 4. Boş yapıcı metod (JPA için gereklidir).
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 5. ID otomatik artsın (1, 2, 3...).
    private Long id;
    private String skuCode;
    private Integer quantity;
}