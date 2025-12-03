package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDto;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TableMapper {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;

    public RestaurantTable toEntity(TableCreateRequest tableCreateRequest, UUID restaurantId) {
        if (tableRepository.existsByTableNumber(tableCreateRequest.getTableNumber())){
            throw new IllegalArgumentException("Table number already exists");
        }
        RestaurantTable restaurantTable = new RestaurantTable();
        if (tableCreateRequest.getTableNumber()!=null && !tableCreateRequest.getTableNumber().isBlank()){
            restaurantTable.setTableNumber(tableCreateRequest.getTableNumber());
        }
        if (tableCreateRequest.getTableDescription()!=null && !tableCreateRequest.getTableDescription().isBlank()){
            restaurantTable.setTableDescription(tableCreateRequest.getTableDescription());
        }
        if (tableCreateRequest.getTableShape()!=null && !tableCreateRequest.getTableShape().isBlank()){
            restaurantTable.setTableShape(tableCreateRequest.getTableShape());
        }
        if (tableCreateRequest.getTotalNumberOfSeats()!=null && tableCreateRequest.getTotalNumberOfSeats()>0){
            restaurantTable.setTotalNumberOfSeats(tableCreateRequest.getTotalNumberOfSeats());
        }
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));
        restaurantTable.setRestaurant(restaurant);
        return restaurantTable;

    }

    public RestaurantTableDto toDto(RestaurantTable savedTable) {
        RestaurantTableDto restaurantTableDto = modelMapper.map(savedTable, RestaurantTableDto.class);
        restaurantTableDto.setRestaurantId(savedTable.getRestaurant().getRestaurantId());
        return restaurantTableDto;
    }

    public RestaurantTable toUpdateEntity(RestaurantTable table, TableUpdateRequest tableUpdateRequest) {
        if (tableUpdateRequest.getTableNumber()!=null && !tableUpdateRequest.getTableNumber().isBlank() && !tableUpdateRequest.getTableNumber().equals(table.getTableNumber())){
            if (tableRepository.existsByTableNumber(tableUpdateRequest.getTableNumber())){
                throw new IllegalArgumentException("Table number already exists");
            }
            table.setTableNumber(tableUpdateRequest.getTableNumber());
        }
        if (tableUpdateRequest.getTableDescription()!=null && !tableUpdateRequest.getTableDescription().isBlank() && !tableUpdateRequest.getTableDescription().equals(table.getTableDescription())){
            table.setTableDescription(tableUpdateRequest.getTableDescription());
        }
        if (tableUpdateRequest.getTableShape()!=null && !tableUpdateRequest.getTableShape().isBlank() && !tableUpdateRequest.getTableShape().equals(table.getTableShape())){
            table.setTableShape(tableUpdateRequest.getTableShape());
        }
        if (tableUpdateRequest.getTotalNumberOfSeats()!=null && tableUpdateRequest.getTotalNumberOfSeats()>0 && !tableUpdateRequest.getTotalNumberOfSeats().equals(table.getTotalNumberOfSeats())){
            table.setTotalNumberOfSeats(tableUpdateRequest.getTotalNumberOfSeats());
        }
        return table;
    }
}
