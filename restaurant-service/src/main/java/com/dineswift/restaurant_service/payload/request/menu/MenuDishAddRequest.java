package com.dineswift.restaurant_service.payload.request.menu;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuDishAddRequest {

    private List<UUID> dishIds;

}
