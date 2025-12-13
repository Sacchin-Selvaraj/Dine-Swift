package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.OrderItemException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.TableMapper;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDto;
import com.dineswift.restaurant_service.projection.RestaurantTimings;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TableService {

    private final TableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableMapper tableMapper;
    private final ReservationService reservationService;
    private final AuthService authService;

    @CacheEvict(
            value = "restaurant:getTablesByRestaurantId",
            allEntries = true
    )
    public void addTableToRestaurant(UUID restaurantId, TableCreateRequest tableCreateRequest) {
        log.info("Adding table to restaurant with ID: {}", restaurantId);

        if (!restaurantRepository.existsById(restaurantId)){
            log.error("Restaurant not found with ID: {}", restaurantId);
            throw new RestaurantException("Restaurant not found with ID: " + restaurantId);
        }

        RestaurantTable restaurantTable=tableMapper.toEntity(tableCreateRequest);

        Restaurant existingRestaurant = restaurantRepository.getReferenceById(restaurantId);
        restaurantTable.setRestaurant(existingRestaurant);

        restaurantTable.setCreatedBy(authService.getAuthenticatedId());
        restaurantTable.setLastModifiedBy(authService.getAuthenticatedId());
        RestaurantTable savedTable=tableRepository.save(restaurantTable);

        log.info("Table added with ID: {}", savedTable.getTableId());
    }

    @CacheEvict(
            value = "restaurant:getTablesByRestaurantId",
            allEntries = true
    )
    public void deleteTable(UUID tableId) {
        log.info("Deleting table with ID: {}", tableId);

        RestaurantTable existingTable = tableRepository.findByIdAndIsActive(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID or already inactive" + tableId));

        existingTable.deactivate();
        existingTable.setLastModifiedBy(authService.getAuthenticatedId());

        tableRepository.save(existingTable);

        log.info("Table deleted with ID: {}", tableId);
    }

    @CacheEvict(
            value = "restaurant:getTablesByRestaurantId",
            allEntries = true
    )
    public void updateTable(UUID tableId, TableUpdateRequest tableUpdateRequest) {
        log.info("Updating table with ID:{}", tableId);
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));

        RestaurantTable updatedTable = tableMapper.toUpdateEntity(table, tableUpdateRequest);
        updatedTable.setLastModifiedBy(authService.getAuthenticatedId());

        RestaurantTable savedTable = tableRepository.save(updatedTable);

        log.info("Table updated with ID: {}", savedTable.getTableId());
    }

    @Cacheable(
            value = "restaurant:getTablesByRestaurantId",
            key = "#restaurantId.toString().concat('-').concat(#page).concat('-').concat(#size)",
            unless = "#result == null"
    )
    public CustomPageDto<RestaurantTableDto> getTablesByRestaurantId(UUID restaurantId, int page, int size) {
        log.info("Fetching tables for restaurant with ID: {}", restaurantId);

        if (!restaurantRepository.existsById(restaurantId)){
            log.error("Restaurant not found with Given Id: {}", restaurantId);
            throw new RestaurantException("Restaurant not found with ID: " + restaurantId);
        }

        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Specification<RestaurantTable> spec = getRestaurantTableSpecification(restaurantId);

        Page<RestaurantTable> restaurantTables = tableRepository.findAll(spec,pageable);

        if (restaurantTables.isEmpty()) {
            log.warn("No tables found for restaurant with ID: {}", restaurantId);
            throw new IllegalArgumentException("No tables found for restaurant with ID: " + restaurantId);
        }
        log.info("Fetched {} tables for restaurant with ID: {}", restaurantTables.getTotalElements(), restaurantId);
        Page<RestaurantTableDto> restaurantTableDtos = tableMapper.toPageDto(restaurantTables,restaurantId);

        return new CustomPageDto<>(restaurantTableDtos);
    }

    public List<AvailableSlots> getAvailableSlots(UUID restaurantId, CheckAvailableSlots checkAvailableSlots) {

        log.info("Fetching available slots for restaurant with ID: {}", restaurantId);
        List<AvailableSlots> availableSlots = reservationService.getAvailableSlots(restaurantId, checkAvailableSlots);

        if (availableSlots.isEmpty()) {
            log.warn("No available slots found for restaurant with ID: {}", restaurantId);
            throw new RestaurantException("No available slots found for restaurant with ID: " + restaurantId);
        }

        log.info("Fetched available slots for restaurant with ID: {}", restaurantId);
        return availableSlots;

    }

    public AvailableSlots getAvailableSlot(UUID tableId, CheckAvailableSlots checkAvailableSlots) {
        log.info("Fetching available slot for table with ID: {}", tableId);

        RestaurantTable restaurantTable = tableRepository.findByIdAndIsActiveWithRestaurant(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));

        if (!restaurantTable.getIsActive()) {
            log.warn("Table with ID: {} is not active", tableId);
            throw new IllegalArgumentException("Table with ID: " + tableId + " is not active");
        }

        RestaurantTimings restaurantTimings = restaurantRepository
                .findRestaurantTimingsById(restaurantTable.getRestaurant().getRestaurantId());

        AvailableSlots availableSlot = reservationService.getAvailableSlot(restaurantTable,
                restaurantTimings , checkAvailableSlots);

        log.info("Fetched available slot for table with ID: {}", tableId);
        return availableSlot;
    }

    public CustomPageDto<RestaurantTableDto> getTablesByOrderItem(UUID orderItemId, int page, int size) {
        
        log.info("Fetching tables for order item with ID: {}", orderItemId);
        UUID restaurantId = orderItemRepository.findRestaurantIdByOrderItemId(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order item not found with ID: " + orderItemId));

        Specification<RestaurantTable> spec = getRestaurantTableSpecification(restaurantId);

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        
        Page<RestaurantTable> restaurantTables = tableRepository.findAll(spec, pageable);
        if (restaurantTables.isEmpty()) {
            log.warn("No tables found for order item with ID: {}", orderItemId);
            throw new OrderItemException("No tables found for order item with ID: " + orderItemId);
        }
        log.info("Fetched {} tables for order item with ID: {}", restaurantTables.getTotalElements(), orderItemId);
        Page<RestaurantTableDto> restaurantTableDtos = tableMapper.toPageDto(restaurantTables,restaurantId);

        return new CustomPageDto<>(restaurantTableDtos);
    }

    @NotNull
    private static Specification<RestaurantTable> getRestaurantTableSpecification(UUID restaurantId) {
        Specification<RestaurantTable> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurant").get("restaurantId"), restaurantId);
        log.debug("Specification created for Restaurant ID: {}", restaurantId);
        return spec;
    }
}
