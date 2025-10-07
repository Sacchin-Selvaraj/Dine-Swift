package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-items")
@Slf4j
public class OrderItemController {

    private final OrderService orderService;
}
