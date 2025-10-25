package com.dineswift.restaurant_service.geocoding.payload;

import java.util.List;

public class GeocodingResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }
    public void setResults(List<Result> results) {
        this.results = results;
    }

    public static class Result {
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }
        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    public static class Geometry {
        private Coordinate location;

        public Coordinate getLocation() {
            return location;
        }
        public void setLocation(Coordinate location) {
            this.location = location;
        }
    }

    public static class Coordinate {
        private Double lat;
        private Double lng;

        public Double getLat() {
            return lat;
        }
        public void setLat(Double lat) {
            this.lat = lat;
        }
        public Double getLng() {
            return lng;
        }
        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}