package com.management.inventory_management.service;


import com.management.inventory_management.dto.OrderDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.exception.InsufficientStockException;
import com.management.inventory_management.model.Item;
import com.management.inventory_management.model.Order;
import com.management.inventory_management.repository.ItemRepository;
import com.management.inventory_management.repository.OrderRepository;
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
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(100.0);

        testOrder = new Order();
        testOrder.setOrderNo(001L);
        testOrder.setItem(testItem);
        testOrder.setQty(2);
        testOrder.setPrice(100.0);
        testOrder.setTotalPrice(200.0);

        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderNo(001L);
        testOrderDTO.setItemId(1L);
        testOrderDTO.setQty(2);
        testOrderDTO.setPrice(100.0);
    }

    @Test
    void createOrder_Success() {
        when(orderRepository.existsById(001L)).thenReturn(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(10);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO result = orderService.createOrder(testOrderDTO);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNo()).isEqualTo(testOrderDTO.getOrderNo());
        verify(stockService).updateStock(eq(1L), eq(2), eq(false));
    }

    @Test
    void createOrder_InsufficientStock() {
        when(orderRepository.existsById(001L)).thenReturn(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(1);

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(testOrderDTO));
    }

    @Test
    void getAllOrders_Success() {
        Page<Order> page = new PageImpl<>(Arrays.asList(testOrder));
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponseDTO<OrderDTO> result = orderService.getAllOrders(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateOrder_Success() {
        when(orderRepository.findById(001L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(stockService.getCurrentStock(1L)).thenReturn(10);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setQty(3);
        updateDTO.setPrice(100.0);
        updateDTO.setItemId(1L);

        OrderDTO result = orderService.updateOrder(001L, updateDTO);

        assertThat(result).isNotNull();
        verify(stockService).updateStock(eq(1L), eq(1), eq(false)); // Difference of 1 unit
    }

    @Test
    void deleteOrder_Success() {
        when(orderRepository.findById(001L)).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(001L);

        verify(stockService).updateStock(eq(1L), eq(2), eq(true));
        verify(orderRepository).delete(testOrder);
    }
}
