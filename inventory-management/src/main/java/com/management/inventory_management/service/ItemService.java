package com.management.inventory_management.service;

import com.management.inventory_management.dto.ItemDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockService stockService;

    // Method untuk membuat item baru
    public ItemDTO createItem(ItemDTO itemDTO) {
        // Konversi dari DTO ke Entity
        Item item = new Item();
        item.setName(itemDTO.getName());
        item.setPrice(itemDTO.getPrice());
        item.setCurrentStock(itemDTO.getCurrentStock());

        // Simpan ke database
        item = itemRepository.save(item);

        // Konversi balik ke DTO untuk response
        ItemDTO response = new ItemDTO();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setPrice(item.getPrice());
        response.setCurrentStock(item.getCurrentStock());
        return response;
    }

    public ItemDTO getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan"));
        ItemDTO dto = convertToDTO(item);
        return dto;
    }

    public PageResponseDTO<ItemDTO> getAllItems(int pageNo, int pageSize) {
        Page<Item> page = itemRepository.findAll(PageRequest.of(pageNo, pageSize));

        PageResponseDTO<ItemDTO> response = new PageResponseDTO<>();
        response.setContent(page.getContent().stream()
                .map(item -> {
                    ItemDTO dto = convertToDTO(item);
                    return dto;
                })
                .toList());
        response.setPageNo(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }

    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan"));

        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setCurrentStock(dto.getCurrentStock());
        item = itemRepository.save(item);
        ItemDTO response = convertToDTO(item);
        return response;
    }

    // Method untuk mendapatkan semua item
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan"));

        Integer currentStock = stockService.getCurrentStock(id);
        if (currentStock > 0) {
            throw new IllegalStateException(
                    "Tidak dapat menghapus item dengan stok yang ada. Stok saat ini: " + currentStock
            );
        }
        itemRepository.delete(item);
    }

    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setCurrentStock(item.getCurrentStock());
        return dto;
    }

}
