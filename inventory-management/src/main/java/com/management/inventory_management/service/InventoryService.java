package com.management.inventory_management.service;

import com.management.inventory_management.dto.InventoryDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Inventory;
import com.management.inventory_management.model.InventoryType;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.InventoryRepository;
import com.management.inventory_management.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockService stockService;

    // Get single inventory
    public InventoryDTO getInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory tidak ditemukan dengan id: " + id));
        return convertToDTO(inventory);
    }

    // Get all inventory with pagination
    public PageResponseDTO<InventoryDTO> getAllInventory(int pageNo, int pageSize) {
        Page<Inventory> inventoryPage = inventoryRepository.findAll(PageRequest.of(pageNo, pageSize));

        PageResponseDTO<InventoryDTO> response = new PageResponseDTO<>();
        response.setContent(inventoryPage.getContent().stream()
                .map(this::convertToDTO)
                .toList());
        response.setPageNo(inventoryPage.getNumber());
        response.setPageSize(inventoryPage.getSize());
        response.setTotalElements(inventoryPage.getTotalElements());
        response.setTotalPages(inventoryPage.getTotalPages());
        response.setLast(inventoryPage.isLast());
        return response;
    }


    // Create new inventory transaction
    @Transactional
    public InventoryDTO createInventory(InventoryDTO dto) {
        validateInventoryInput(dto);

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan dengan id: " + dto.getItemId()));

        // Check stock for Withdrawal
        if (dto.getType() == InventoryType.W) {
            Integer currentStock = stockService.getCurrentStock(item.getId());
            if (currentStock < dto.getQty()) {
                throw new InsufficientStockException(
                        String.format("Stok tidak mencukupi untuk penarikan. Stok saat ini: %d, Diminta: %d",
                                currentStock, dto.getQty())
                );
            }
        }

        // Update stock
        boolean isAddition = dto.getType() == InventoryType.T;
        stockService.updateStock(item.getId(), dto.getQty(), isAddition);

        // Create inventory record
        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(dto.getQty());
        inventory.setType(dto.getType());
        inventory.setTransactionDate(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);
        return convertToDTO(inventory);
    }

    // Update inventory transaction
    @Transactional
    public InventoryDTO updateInventory(Long id, InventoryDTO dto) {
        validateInventoryInput(dto);

        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory tidak ditemukan dengan id: " + id));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan dengan id: " + dto.getItemId()));

        // Reverse previous stock operation
        boolean wasAddition = existingInventory.getType() == InventoryType.T;
        stockService.updateStock(
                existingInventory.getItem().getId(),
                existingInventory.getQty(),
                !wasAddition
        );

        // Check stock for new Withdrawal
        if (dto.getType() == InventoryType.W) {
            Integer currentStock = stockService.getCurrentStock(item.getId());
            if (currentStock < dto.getQty()) {
                // Rollback the previous reversal
                stockService.updateStock(
                        existingInventory.getItem().getId(),
                        existingInventory.getQty(),
                        wasAddition
                );
                throw new InsufficientStockException(
                        String.format("Stok tidak mencukupi untuk penarikan. Stok saat ini: %d, Diminta: %d",
                                currentStock, dto.getQty())
                );
            }
        }

        // Apply new stock operation
        boolean isAddition = dto.getType() == InventoryType.T;
        stockService.updateStock(item.getId(), dto.getQty(), isAddition);

        // Update inventory record
        existingInventory.setItem(item);
        existingInventory.setQty(dto.getQty());
        existingInventory.setType(dto.getType());
        existingInventory.setTransactionDate(LocalDateTime.now());
        existingInventory = inventoryRepository.save(existingInventory);
        return convertToDTO(existingInventory);
    }

    // Delete inventory transaction
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory tidak ditemukan dengan id: " + id));
        boolean wasAddition = inventory.getType() == InventoryType.T;
        stockService.updateStock(
                inventory.getItem().getId(),
                inventory.getQty(),
                !wasAddition
        );
        inventoryRepository.delete(inventory);
    }

    private void validateInventoryInput(InventoryDTO dto) {
        if (dto.getQty() <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih besar dari 0");
        }
        if (dto.getType() == null) {
            throw new IllegalArgumentException("Jenis Inventory harus ditentukan");
        }
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setItemId(inventory.getItem().getId());
        dto.setQty(inventory.getQty());
        dto.setType(inventory.getType());
        dto.setTransactionDate(inventory.getTransactionDate());
        return dto;
    }
}
