package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDto;
import com.dineswift.restaurant_service.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TableMapper {

    private final TableRepository tableRepository;
    private final ModelMapper modelMapper;

    public RestaurantTable toEntity(TableCreateRequest tableCreateRequest) {
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
        return restaurantTable;

    }

    public RestaurantTable toUpdateEntity(RestaurantTable table, TableUpdateRequest tableUpdateRequest) {

        if (tableUpdateRequest.getTableNumber()!=null && !tableUpdateRequest.getTableNumber().isBlank()
                && !tableUpdateRequest.getTableNumber().equals(table.getTableNumber())){

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

    public Page<RestaurantTableDto> toPageDto(Page<RestaurantTable> restaurantTables, UUID restaurantId) {

        return restaurantTables.map(restaurantTable ->
                toDtoWithRestaurantId(restaurantTable,restaurantId));

    }

    public RestaurantTableDto toDtoWithRestaurantId(RestaurantTable restaurantTable, UUID restaurantId) {
        RestaurantTableDto restaurantTableDto = modelMapper.map(restaurantTable, RestaurantTableDto.class);
        restaurantTableDto.setRestaurantId(restaurantId);
        return restaurantTableDto;
    }
}
