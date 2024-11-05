package com.management.inventory_management.dto;

import lombok.Data;

@Data
public class ItemDTO {

    private Long id;
    private String name;
    private Double price;
    private Integer currentStock;


}
