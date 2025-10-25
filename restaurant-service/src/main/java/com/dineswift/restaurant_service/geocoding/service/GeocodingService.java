package com.dineswift.restaurant_service.geocoding.service;

import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.geocoding.payload.GeocodingResponse;
import com.dineswift.restaurant_service.model.Coordinates;
import com.google.maps.model.GeocodingResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GeocodingService {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String apiKey;

    private final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json?address={address}&key={apiKey}";

    public Coordinates getCoordinates(String address) {
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("address", address);
        uriVariables.put("apiKey", apiKey);

        try {
            ResponseEntity<GeocodingResponse> response = restTemplate.getForEntity(GOOGLE_API_URL, GeocodingResponse.class,uriVariables);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GeocodingResponse geocodingResponse = response.getBody();
                if (geocodingResponse.getResults() != null && !geocodingResponse.getResults().isEmpty()) {
                    GeocodingResponse.Coordinate location = geocodingResponse.getResults().get(0).getGeometry().getLocation();
                    Coordinates coords = new Coordinates();
                    coords.setLatitude(BigDecimal.valueOf(location.getLat()));
                    coords.setLongitude(BigDecimal.valueOf(location.getLng()));
                    return coords;
                }
            }
        } catch (Exception e) {
            throw new RestaurantException("Failed to fetch coordinates from Geocoding API"+e.getMessage());
        }
        return null;
    }
}
