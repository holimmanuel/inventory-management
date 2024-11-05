package com.management.inventory_management.service;

import com.management.inventory_management.dto.OrderDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.model.Order;
import com.management.inventory_management.repository.ItemRepository;
import com.management.inventory_management.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockService stockService;

    public OrderDTO getOrder(Long orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new EntityNotFoundException("Pesanan tidak ditemukan dengan nomor: " + orderNo));
        return convertToDTO(order);
    }

    public PageResponseDTO<OrderDTO> getAllOrders(int pageNo, int pageSize) {
        Page<Order> orderPage = orderRepository.findAll(PageRequest.of(pageNo, pageSize));

        PageResponseDTO<OrderDTO> response = new PageResponseDTO<>();
        response.setContent(orderPage.getContent().stream()
                .map(this::convertToDTO)
                .toList());
        response.setPageNo(orderPage.getNumber());
        response.setPageSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        response.setLast(orderPage.isLast());
        return response;
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        // Validate order number uniqueness
        if (orderRepository.existsById(orderDTO.getOrderNo())) {
            throw new IllegalStateException("Nomor pesanan sudah ada: " + orderDTO.getOrderNo());
        }

        Item item = itemRepository.findById(orderDTO.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan dengan id: " + orderDTO.getItemId()));

        Integer currentStock = stockService.getCurrentStock(item.getId());
        if (currentStock < orderDTO.getQty()) {
            throw new InsufficientStockException(
                    String.format("Stok barang tidak mencukupi %s. Diperlukan: %d, Tersedia: %d",
                            item.getName(), orderDTO.getQty(), currentStock)
            );
        }

        Order order = new Order();
        order.setOrderNo(orderDTO.getOrderNo());
        order.setItem(item);
        order.setQty(orderDTO.getQty());
        order.setPrice(orderDTO.getPrice());
        order.setTotalPrice(orderDTO.getQty() * orderDTO.getPrice());
        stockService.updateStock(item.getId(), orderDTO.getQty(), false);
        order = orderRepository.save(order);
        return convertToDTO(order);

    }

    @Transactional
    public OrderDTO updateOrder(Long orderNo, OrderDTO orderDTO) {
        Order existingOrder = orderRepository.findById(orderNo)
                .orElseThrow(() -> new EntityNotFoundException("Pesanan tidak ditemukan dengan nomor: " + orderNo));

        Item item = itemRepository.findById(orderDTO.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item tidak ditemukan dengan id: " + orderDTO.getItemId()));
        int stockDifference = orderDTO.getQty() - existingOrder.getQty();
        if (stockDifference > 0) {
            Integer currentStock = stockService.getCurrentStock(item.getId());
            if (currentStock < stockDifference) {
                throw new InsufficientStockException(
                        String.format("Stok barang tidak mencukupi %s. Diperlukan required: %d, Tersedia: %d",
                                item.getName(), stockDifference, currentStock)
                );
            }
        }
        // Update stock
        if (stockDifference != 0) {
            stockService.updateStock(item.getId(), Math.abs(stockDifference), stockDifference < 0);
        }
        // Update order
        existingOrder.setItem(item);
        existingOrder.setQty(orderDTO.getQty());
        existingOrder.setPrice(orderDTO.getPrice());
        existingOrder.setTotalPrice(orderDTO.getQty() * orderDTO.getPrice());
        existingOrder = orderRepository.save(existingOrder);
        return convertToDTO(existingOrder);
    }

    // Delete order
    @Transactional
    public void deleteOrder(Long orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with number: " + orderNo));
        stockService.updateStock(order.getItem().getId(), order.getQty(), true);
        orderRepository.delete(order);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderNo(order.getOrderNo());
        dto.setItemId(order.getItem().getId());
        dto.setQty(order.getQty());
        dto.setPrice(order.getPrice());
        dto.setTotalPrice(order.getTotalPrice());
        return dto;
    }

}
