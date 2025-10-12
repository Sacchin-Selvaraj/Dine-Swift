package com.dineswift.userservice.controller;

import com.dineswift.userservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

//    @PostMapping("/book-table/{cartId}" )

}
