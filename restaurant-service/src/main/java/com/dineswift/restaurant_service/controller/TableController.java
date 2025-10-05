package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.request.table.TableCreateRequest;
import com.dineswift.restaurant_service.payload.request.table.TableUpdateRequest;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDTO;
import com.dineswift.restaurant_service.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/table")
@Slf4j
public class TableController {

    private final TableService tableService;

    @PostMapping("/add-table/{restaurantId}" )
    public ResponseEntity<RestaurantTableDTO> addTableToRestaurant(@PathVariable UUID  restaurantId, @RequestBody TableCreateRequest tableCreateRequest) {
        log.info("Received request to add table to restaurant with ID: {}", restaurantId);
        RestaurantTableDTO createdTable = tableService.addTableToRestaurant(restaurantId, tableCreateRequest);
        return ResponseEntity.ok(createdTable);
    }

    @DeleteMapping("/delete-table/{tableId}" )
    public ResponseEntity<Void> deleteTable(@PathVariable UUID  tableId) {
        log.info("Received request to delete table with ID: {}", tableId);
        tableService.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-table/{tableId}" )
    public ResponseEntity<RestaurantTableDTO> updateTable(@PathVariable UUID  tableId, @RequestBody TableUpdateRequest tableUpdateRequest) {
        log.info("Received request to update table with ID: {}", tableId);
        RestaurantTableDTO updatedTable = tableService.updateTable(tableId, tableUpdateRequest);
        return ResponseEntity.ok().body(updatedTable);
    }

    @GetMapping("/get-table/{restaurantId}" )
    public ResponseEntity<Page<RestaurantTableDTO>> getTable(@PathVariable UUID  restaurantId,
                                         @RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Received request to get table with ID: {}", restaurantId);
        Page<RestaurantTableDTO> tables = tableService.getTablesByRestaurantId(restaurantId, page, size);
        return ResponseEntity.ok().body(tables);
    }

    @GetMapping("/available-slots/{restaurantId}" )
    public ResponseEntity<List<AvailableSlots>> getAvailableSlots(@PathVariable UUID  restaurantId, @RequestBody CheckAvailableSlots checkAvailableSlots) {
        log.info("Received request to get available slots for table with ID: {}", restaurantId);
        List<AvailableSlots> availableSlots = tableService.getAvailableSlots(restaurantId,checkAvailableSlots);
        return ResponseEntity.ok().body(availableSlots);
    }
}
