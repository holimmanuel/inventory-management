package com.management.inventory_management.dto;

import com.management.inventory_management.model.InventoryType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryDTO {

    private Long id;
    private Long itemId;
    private Integer qty;
    private InventoryType type;
    private LocalDateTime transactionDate;
}
