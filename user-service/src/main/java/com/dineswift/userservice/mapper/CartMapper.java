package com.dineswift.userservice.mapper;

import com.dineswift.userservice.model.entites.Cart;
import com.dineswift.userservice.model.response.CartDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartMapper {

    private final ModelMapper modelMapper;


    public CartDTO toDto(Cart cart) {
        log.info("Mapping Cart entity to CartDTO: {}", cart);
        return modelMapper.map(cart, CartDTO.class);
    }
}
