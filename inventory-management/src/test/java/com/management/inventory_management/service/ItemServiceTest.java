package com.management.inventory_management.service;

import com.management.inventory_management.dto.ItemDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private ItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100.0);
        testItem.setCurrentStock(10);

        testItemDTO = new ItemDTO();
        testItemDTO.setName("Test Item");
        testItemDTO.setPrice(100.0);
    }

    @Test
    void createItem_Success() {
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        ItemDTO result = itemService.createItem(testItemDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testItem.getId());
        assertThat(result.getName()).isEqualTo(testItem.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void getItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        ItemDTO result = itemService.getItem(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testItem.getId());
        assertThat(result.getCurrentStock()).isEqualTo(10);
    }

    @Test
    void getItem_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(1L));
    }

    @Test
    void getAllItems_Success() {
        Page<Item> page = new PageImpl<>(Arrays.asList(testItem));
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponseDTO<ItemDTO> result = itemService.getAllItems(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        ItemDTO result = itemService.updateItem(1L, testItemDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testItemDTO.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void deleteItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(0);

        itemService.deleteItem(1L);

        verify(itemRepository).delete(testItem);
    }

    @Test
    void deleteItem_WithStock_ThrowsException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(10);

        assertThrows(IllegalStateException.class, () -> itemService.deleteItem(1L));

        verify(itemRepository, never()).delete(any(Item.class));
    }
}
