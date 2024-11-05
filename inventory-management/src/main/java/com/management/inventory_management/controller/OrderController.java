package com.management.inventory_management.controller;

import com.management.inventory_management.dto.OrderDTO;
import com.management.inventory_management.dto.PageResponseDTO;
import com.management.inventory_management.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Get single order
    @GetMapping("/{orderNo}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderNo) {
        return ResponseEntity.ok(orderService.getOrder(orderNo));
    }

    // Get all orders with pagination
    @GetMapping
    public ResponseEntity<PageResponseDTO<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(orderService.getAllOrders(pageNo, pageSize));
    }

    // Create new order
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    // Update order
    @PutMapping("/{orderNo}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long orderNo,
            @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.updateOrder(orderNo, orderDTO));
    }
}
