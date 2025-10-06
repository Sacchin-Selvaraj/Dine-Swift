package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.mapper.TableMapper;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDTO;
import com.dineswift.restaurant_service.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TableService {

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final ReservationService reservationService;

    public RestaurantTableDTO addTableToRestaurant(UUID restaurantId, TableCreateRequest tableCreateRequest) {
        log.info("Adding table to restaurant with ID: {}", restaurantId);
        RestaurantTable restaurantTable=tableMapper.toEntity(tableCreateRequest,restaurantId);
        restaurantTable.setCreatedBy(restaurantId);
        // need to set createdBy and lastModifiedBy from the authenticated user context
        RestaurantTable savedTable=tableRepository.save(restaurantTable);
        log.info("Table added with ID: {}", savedTable.getTableId());
        return tableMapper.toDto(savedTable);

    }

    public void deleteTable(UUID tableId) {
        log.info("Deleting table with ID: {}", tableId);
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));
        table.setIsActive(false);
        tableRepository.save(table);
        log.info("Table deleted with ID: {}", tableId);
    }

    public RestaurantTableDTO updateTable(UUID tableId, TableUpdateRequest tableUpdateRequest) {
        log.info("Updating table with ID:{}", tableId);
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));

        RestaurantTable updatedTable = tableMapper.toUpdateEntity(table, tableUpdateRequest);
        // need to set lastModifiedBy from the authenticated user context
        updatedTable.setLastModifiedBy(table.getCreatedBy());
        RestaurantTable savedTable = tableRepository.save(updatedTable);
        log.info("Table updated with ID: {}", savedTable.getTableId());
        return tableMapper.toDto(savedTable);
    }

    public Page<RestaurantTableDTO> getTablesByRestaurantId(UUID restaurantId, int page, int size) {
        log.info("Fetching tables for restaurant with ID: {}", restaurantId);

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<RestaurantTable> restaurantTables = tableRepository.findAll(pageable);
        if (restaurantTables.isEmpty()) {
            log.warn("No tables found for restaurant with ID: {}", restaurantId);
            throw new IllegalArgumentException("No tables found for restaurant with ID: " + restaurantId);
        }
        log.info("Fetched {} tables for restaurant with ID: {}", restaurantTables.getTotalElements(), restaurantId);
        return restaurantTables.map(tableMapper::toDto);
    }

    public List<AvailableSlots> getAvailableSlots(UUID restaurantId, CheckAvailableSlots checkAvailableSlots) {

        log.info("Fetching available slots for restaurant with ID: {}", restaurantId);
        List<AvailableSlots> availableSlots = reservationService.getAvailableSlots(restaurantId, checkAvailableSlots);
        if (availableSlots.isEmpty()) {
            log.warn("No available slots found for restaurant with ID: {}", restaurantId);
            throw new IllegalArgumentException("No available slots found for restaurant with ID: " + restaurantId);
        }
        log.info("Fetched available slots for restaurant with ID: {}", restaurantId);
        return availableSlots;

    }

    public AvailableSlots getAvailableSlot(UUID tableId, CheckAvailableSlots checkAvailableSlots) {
        log.info("Fetching available slot for table with ID: {}", tableId);
        RestaurantTable restaurantTable = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));
        if (!restaurantTable.getIsActive()) {
            log.warn("Table with ID: {} is not active", tableId);
            throw new IllegalArgumentException("Table with ID: " + tableId + " is not active");
        }
        AvailableSlots availableSlot = reservationService.getAvailableSlot(restaurantTable, restaurantTable.getRestaurant(), checkAvailableSlots);
        log.info("Fetched available slot for table with ID: {}", tableId);
        return availableSlot;
    }
}
