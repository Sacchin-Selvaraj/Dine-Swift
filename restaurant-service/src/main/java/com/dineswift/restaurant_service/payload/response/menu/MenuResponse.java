package com.dineswift.restaurant_service.payload.response.menu;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class MenuResponse {

    List<MenuNameResponse> menuNameResponses;
}
