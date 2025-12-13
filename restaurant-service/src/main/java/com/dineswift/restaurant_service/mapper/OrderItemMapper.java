package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.DishImage;
import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.repository.DishImageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final DishMapper dishMapper;
    private final DishImageRepository dishImageRepository;
    private final ModelMapper modelMapper;


    public OrderItem toEntity(UUID cartId, Dish dish, Integer quantity) {
        OrderItem orderItem=new OrderItem();
        orderItem.setCartId(cartId);
        orderItem.setDish(dish);
        orderItem.setQuantity(quantity);
        orderItem.setRestaurant(dish.getRestaurant());
        return orderItem;
    }

    public OrderItemDto toDtoAfterBooking(OrderItem orderItem) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setOrderItemsId(orderItem.getOrderItemsId());
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setPrice(orderItem.getFrozenPrice());
        orderItemDto.setTotalPrice(orderItem.getFrozenTotalPrice());
        return orderItemDto;
    }

    public List<OrderItemDto> toListDto(List<OrderItem> orderItems) {
        List<Dish> dishes = orderItems
                .stream()
                .map(OrderItem::getDish)
                .distinct()
                .toList();

        List<DishImage> dishImages = dishImageRepository.findByDishes(dishes);

        Map<UUID,List<DishImage>> dishImagesMap = dishImages
                .stream()
                .collect(Collectors.groupingBy(dishImage -> dishImage.getDish().getDishId()));

        return orderItems.stream()
                .map(orderItem -> {

                    OrderItemDto orderItemDto = modelMapper.map(orderItem, OrderItemDto.class);

                    Dish dish = orderItem.getDish();
                    DishDTO dishDTO = modelMapper.map(dish, DishDTO.class);

                    List<DishImage> imagesForDish = dishImagesMap.getOrDefault(dish.getDishId(), List.of());
                    dishDTO.setDishImages(imagesForDish.stream().map(
                            dishMapper::toImageDTO
                    ).toList());

                    orderItemDto.setDish(dishDTO);
                    return orderItemDto;

                }).toList();
    }

    public Map<UUID,OrderItemDto> toListDtoAfterBooking(List<OrderItem> orderItemList) {

        List<Dish> dishes = orderItemList
                .stream()
                .map(OrderItem::getDish)
                .distinct()
                .toList();

        List<DishDTO> dishDTOS = dishMapper.toDTOList(dishes);

        Map<UUID,DishDTO> dishDTOMap = dishDTOS
                .stream()
                .collect(Collectors.toMap(DishDTO::getDishId, dishDTO -> dishDTO));

          return orderItemList.stream()
                .map(orderItem -> {
                    OrderItemDto orderItemDto = this.toDtoAfterBooking(orderItem);

                    orderItemDto.setDish(dishDTOMap.get(orderItem.getDish().getDishId()));

                    return orderItemDto;

                }).collect(Collectors.toMap(OrderItemDto::getOrderItemsId,
                          orderItemDto -> orderItemDto));

    }
}
