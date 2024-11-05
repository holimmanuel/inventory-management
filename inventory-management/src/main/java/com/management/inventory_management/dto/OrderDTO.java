package com.management.inventory_management.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long orderNo;
    private Long itemId;
    private Integer qty;
    private Double price;
    private Double totalPrice;
}
