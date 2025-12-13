package com.dineswift.restaurant_service.geocoding.service;

import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.geocoding.payload.GeocodingResponse;
import com.dineswift.restaurant_service.model.Coordinates;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String apiKey;

    public Coordinates getCoordinates(String address) {
        log.info("Fetching coordinates for address: {}", address);
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("address", address);
        uriVariables.put("apiKey", apiKey);

        try {
            String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json?address={address}&key={apiKey}";
            ResponseEntity<GeocodingResponse> response = restTemplate.getForEntity(GOOGLE_API_URL, GeocodingResponse.class,uriVariables);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GeocodingResponse geocodingResponse = response.getBody();
                if (geocodingResponse.getResults() != null && !geocodingResponse.getResults().isEmpty()) {
                    GeocodingResponse.Location  location = geocodingResponse.getResults().getFirst().getGeometry().getLocation();
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
