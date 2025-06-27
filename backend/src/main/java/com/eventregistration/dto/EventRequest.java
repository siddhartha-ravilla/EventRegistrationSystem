package com.eventregistration.dto;

import com.eventregistration.model.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventRequest {
    
    @NotBlank(message = "Event title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Event location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    @NotNull(message = "Start date and time is required")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "End date and time is required")
    private LocalDateTime endDateTime;
    
    @NotNull(message = "Event capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;
    
    @NotNull(message = "Event price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String imageUrl;
    
    private String category;
    
    // Constructors
    public EventRequest() {}
    
    public EventRequest(String title, String description, String location, LocalDateTime startDateTime, 
                       LocalDateTime endDateTime, Integer capacity, BigDecimal price, String imageUrl, String category) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.capacity = capacity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }
    
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    
    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
} 