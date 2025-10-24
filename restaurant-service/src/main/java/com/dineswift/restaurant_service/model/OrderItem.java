package com.dineswift.restaurant_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@RequiredArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_items_id", nullable = false, updatable = false)
    private UUID orderItemsId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Total price must be greater than 0")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @DecimalMin(value = "0.01", inclusive = true, message = "Frozen Price must be greater than 0")
    @Column(name = "frozen_price",  precision = 10, scale = 2)
    private BigDecimal frozenPrice;

    @DecimalMin(value = "0.01", inclusive = true, message = "Frozen Total price must be greater than 0")
    @Column(name = "frozen_totalprice", precision = 10, scale = 2)
    private BigDecimal frozenTotalPrice;

    @NotNull(message = "isBooked flag is required")
    @Column(name = "is_booked", nullable = false)
    private boolean isBooked;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @NotNull(message = "Cart is required")
    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @OneToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "table_booking_id")
    @JsonIgnore
    private TableBooking tableBooking;


    @PrePersist
    @PreUpdate
    @PostLoad
    protected void refreshPrices(){
        BigDecimal discountRate = dish.getDiscount().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = dish.getDishPrice().multiply(discountRate);
        this.price = dish.getDishPrice().subtract(discountAmount);
        this.totalPrice=this.price.multiply(BigDecimal.valueOf(this.quantity));
    }

    public void setFrozenValues(){
        this.frozenPrice=this.price;
        this.frozenTotalPrice=this.totalPrice;
        this.isBooked=true;
    }

    public BigDecimal getPrice() {
        return isBooked ? frozenPrice : price;
    }

    public BigDecimal getTotalPrice() {
        return isBooked ? frozenTotalPrice : totalPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        refreshPrices();
    }
}