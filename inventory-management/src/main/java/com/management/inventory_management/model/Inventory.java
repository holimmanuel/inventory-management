package com.management.inventory_management.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private int qty;

    @Enumerated(EnumType.STRING)
    private InventoryType type;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();
}
