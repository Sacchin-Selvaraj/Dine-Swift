package com.dineswift.restaurant_service.geocoding.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class GeocodingResponse {
    private List<Result> results;
    private String status;

    public List<Result> getResults() { return results; }
    public void setResults(List<Result> results) { this.results = results; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static class Result {
        @JsonProperty("address_components")
        private List<AddressComponent> addressComponents;

        @JsonProperty("formatted_address")
        private String formattedAddress;

        private Geometry geometry;

        @JsonProperty("partial_match")
        private Boolean partialMatch;

        @JsonProperty("place_id")
        private String placeId;

        private List<String> types;

        public List<AddressComponent> getAddressComponents() { return addressComponents; }
        public void setAddressComponents(List<AddressComponent> addressComponents) { this.addressComponents = addressComponents; }
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
        public Boolean getPartialMatch() { return partialMatch; }
        public void setPartialMatch(Boolean partialMatch) { this.partialMatch = partialMatch; }
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }

    public static class AddressComponent {
        @JsonProperty("long_name")
        private String longName;

        @JsonProperty("short_name")
        private String shortName;

        private List<String> types;

        public String getLongName() { return longName; }
        public void setLongName(String longName) { this.longName = longName; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }

    public static class Geometry {
        private Bounds bounds;
        private Location location;

        @JsonProperty("location_type")
        private String locationType;

        private Viewport viewport;

        public Bounds getBounds() { return bounds; }
        public void setBounds(Bounds bounds) { this.bounds = bounds; }
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
        public String getLocationType() { return locationType; }
        public void setLocationType(String locationType) { this.locationType = locationType; }
        public Viewport getViewport() { return viewport; }
        public void setViewport(Viewport viewport) { this.viewport = viewport; }
    }

    public static class Bounds {
        private LatLng northeast;
        private LatLng southwest;

        public LatLng getNortheast() { return northeast; }
        public void setNortheast(LatLng northeast) { this.northeast = northeast; }
        public LatLng getSouthwest() { return southwest; }
        public void setSouthwest(LatLng southwest) { this.southwest = southwest; }
    }

    public static class Viewport {
        private LatLng northeast;
        private LatLng southwest;

        public LatLng getNortheast() { return northeast; }
        public void setNortheast(LatLng northeast) { this.northeast = northeast; }
        public LatLng getSouthwest() { return southwest; }
        public void setSouthwest(LatLng southwest) { this.southwest = southwest; }
    }

    public static class Location {
        private double lat;
        private double lng;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }

    public static class LatLng {
        private double lat;
        private double lng;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
    }

    public static GeocodingResponse fromJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, GeocodingResponse.class);
    }

    public String toJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}