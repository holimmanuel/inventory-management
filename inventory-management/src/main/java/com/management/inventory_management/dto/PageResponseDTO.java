package com.management.inventory_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponseDTO <T>{

    private List<T> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
