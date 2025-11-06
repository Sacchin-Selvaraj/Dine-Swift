package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDto;
import com.dineswift.restaurant_service.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant/table")
@Slf4j
public class TableController {

    private final TableService tableService;

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')"))
    @PostMapping("/add-table/{restaurantId}" )
    public ResponseEntity<RestaurantTableDto> addTableToRestaurant(@PathVariable UUID  restaurantId, @RequestBody TableCreateRequest tableCreateRequest) {
        log.info("Received request to add table to restaurant with ID: {}", restaurantId);
        RestaurantTableDto createdTable = tableService.addTableToRestaurant(restaurantId, tableCreateRequest);
        return ResponseEntity.ok(createdTable);
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')"))
    @DeleteMapping("/delete-table/{tableId}" )
    public ResponseEntity<Void> deleteTable(@PathVariable UUID  tableId) {
        log.info("Received request to delete table with ID: {}", tableId);
        tableService.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')"))
    @PatchMapping("/update-table/{tableId}" )
    public ResponseEntity<RestaurantTableDto> updateTable(@PathVariable UUID  tableId, @RequestBody TableUpdateRequest tableUpdateRequest) {
        log.info("Received request to update table with ID: {}", tableId);
        RestaurantTableDto updatedTable = tableService.updateTable(tableId, tableUpdateRequest);
        return ResponseEntity.ok().body(updatedTable);
    }

    @GetMapping("/get-table/{restaurantId}" )
    public ResponseEntity<Page<RestaurantTableDto>> getTable(@PathVariable UUID  restaurantId,
                                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                                             @RequestParam(name = "size", defaultValue = "6") int size) {
        log.info("Received request to get table with ID: {}", restaurantId);
        Page<RestaurantTableDto> tables = tableService.getTablesByRestaurantId(restaurantId, page, size);
        return ResponseEntity.ok().body(tables);
    }

    @GetMapping("/available-slots/{restaurantId}" )
    public ResponseEntity<List<AvailableSlots>> getAvailableSlots(@PathVariable UUID  restaurantId, @RequestBody CheckAvailableSlots checkAvailableSlots) {
        log.info("Received request to get available slots for table with ID: {}", restaurantId);
        List<AvailableSlots> availableSlots = tableService.getAvailableSlots(restaurantId,checkAvailableSlots);
        return ResponseEntity.ok().body(availableSlots);
    }

    @GetMapping("/available-slot/{tableId}" )
    public ResponseEntity<AvailableSlots> getAvailableSlot(@PathVariable UUID  tableId, @RequestBody CheckAvailableSlots checkAvailableSlots) {
        log.info("Received request to get available slot for table with ID: {}", tableId);
        AvailableSlots availableSlot = tableService.getAvailableSlot(tableId, checkAvailableSlots);
        return ResponseEntity.ok().body(availableSlot);
    }
}
