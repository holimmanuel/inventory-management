package com.management.inventory_management.service;


import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public void updateStock(Long itemId, Integer quantity, boolean isAddition) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan dengan id: " + itemId));

        Integer currentStock = itemRepository.calculateItemStock(itemId);

        int newStock;
        if (isAddition) {
            newStock = currentStock + quantity;
        } else {
            newStock = currentStock - quantity;
            if (newStock < 0) {
                throw new InsufficientStockException(
                        String.format("Stok barang tidak mencukupi %s. Diperlikan: %d, Tersedia: %d",
                                item.getName(), quantity, currentStock)
                );
            }
        }
        item.setCurrentStock(newStock);
        itemRepository.save(item);
    }
    public Integer getCurrentStock(Long itemId) {
        return itemRepository.calculateItemStock(itemId);
    }
}
