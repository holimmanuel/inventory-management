package com.management.inventory_management.service;

import com.management.inventory_management.dto.InventoryDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Inventory;
import com.management.inventory_management.model.InventoryType;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.InventoryRepository;
import com.management.inventory_management.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private InventoryDTO testInventoryDTO;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType(InventoryType.T);
        testInventory.setTransactionDate(LocalDateTime.now());

        testInventoryDTO = new InventoryDTO();
        testInventoryDTO.setItemId(1L);
        testInventoryDTO.setQty(10);
        testInventoryDTO.setType(InventoryType.T);
    }

    @Test
    void createInventory_TopUp_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertThat(result).isNotNull();
        verify(stockService).updateStock(eq(1L), eq(10), eq(true));
    }

    @Test
    void createInventory_Withdrawal_Success() {
        testInventoryDTO.setType(InventoryType.W);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(20);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        assertThat(result).isNotNull();
        verify(stockService).updateStock(eq(1L), eq(10), eq(false));
    }

    @Test
    void createInventory_Withdrawal_InsufficientStock() {
        testInventoryDTO.setType(InventoryType.W);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(5);

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.createInventory(testInventoryDTO));
    }

    @Test
    void getAllInventory_Success() {
        Page<Inventory> page = new PageImpl<>(Arrays.asList(testInventory));
        when(inventoryRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponseDTO<InventoryDTO> result = inventoryService.getAllInventory(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateInventory_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        InventoryDTO updateDTO = new InventoryDTO();
        updateDTO.setItemId(1L);
        updateDTO.setQty(15);
        updateDTO.setType(InventoryType.T);

        InventoryDTO result = inventoryService.updateInventory(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(stockService).updateStock(eq(1L), eq(10), eq(false)); // Reverse original
        verify(stockService).updateStock(eq(1L), eq(15), eq(true));  // Apply new
    }

    @Test
    void updateInventory_WithdrawalInsufficientStock() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(5);

        InventoryDTO updateDTO = new InventoryDTO();
        updateDTO.setItemId(1L);
        updateDTO.setQty(10);
        updateDTO.setType(InventoryType.W);

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.updateInventory(1L, updateDTO));
    }

    @Test
    void deleteInventory_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        inventoryService.deleteInventory(1L);

        verify(stockService).updateStock(eq(1L), eq(10), eq(false)); // Reverse the operation
        verify(inventoryRepository).delete(testInventory);
    }

    @Test
    void deleteInventory_WithdrawalType() {
        testInventory.setType(InventoryType.W);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        inventoryService.deleteInventory(1L);

        verify(stockService).updateStock(eq(1L), eq(10), eq(true)); // Reverse withdrawal
        verify(inventoryRepository).delete(testInventory);
    }

}
