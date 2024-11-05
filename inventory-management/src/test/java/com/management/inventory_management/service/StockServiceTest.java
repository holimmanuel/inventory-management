package com.management.inventory_management.service;


import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private StockService stockService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setCurrentStock(10);
    }

    @Test
    void updateStock_Addition_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.calculateItemStock(1L)).thenReturn(10);

        stockService.updateStock(1L, 5, true);

        verify(itemRepository).save(argThat(item ->
                item.getCurrentStock().equals(15)
        ));
    }

    @Test
    void updateStock_Subtraction_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.calculateItemStock(1L)).thenReturn(10);

        stockService.updateStock(1L, 5, false);

        verify(itemRepository).save(argThat(item ->
                item.getCurrentStock().equals(5)
        ));
    }

    @Test
    void updateStock_InsufficientStock() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.calculateItemStock(1L)).thenReturn(10);

        assertThrows(InsufficientStockException.class, () ->
                stockService.updateStock(1L, 15, false)
        );
    }

    @Test
    void updateStock_ItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                stockService.updateStock(1L, 5, true)
        );
    }

    @Test
    void getCurrentStock_Success() {
        when(itemRepository.calculateItemStock(1L)).thenReturn(10);

        Integer result = stockService.getCurrentStock(1L);

        assertEquals(10, result);
        verify(itemRepository).calculateItemStock(1L);
    }

}
