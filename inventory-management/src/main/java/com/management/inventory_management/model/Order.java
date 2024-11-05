package com.management.inventory_management.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private Long orderNo;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    private int qty;
    private double price;
    @Column(name = "total_price")
    private Double totalPrice;
}
