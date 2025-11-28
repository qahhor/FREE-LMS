package com.freelms.booking.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

/**
 * Bookable resource entity - rooms, equipment, trainers.
 */
@Entity
@Table(name = "resources")
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    // Location info
    private String building;
    private String floor;
    private String roomNumber;
    private String address;
    private Double latitude;
    private Double longitude;

    // Capacity and features
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "resource_features", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "feature")
    private Set<String> features;

    @ElementCollection
    @CollectionTable(name = "resource_equipment", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "equipment")
    private Set<String> includedEquipment;

    // Availability settings
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "resource_available_days", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "day_of_week")
    private Set<DayOfWeek> availableDays;

    private LocalTime availableFrom;
    private LocalTime availableUntil;

    // Booking rules
    private Integer minBookingMinutes = 30;
    private Integer maxBookingMinutes = 480;
    private Integer advanceBookingDays = 30;
    private Integer cancellationNoticeHours = 24;
    private Boolean requiresApproval = false;

    // Pricing
    private Double hourlyRate;
    private String currency = "USD";

    // Organization
    private Long organizationId;
    private Long managerId;

    // Images
    @Column(length = 2000)
    private String imageUrls;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ResourceType {
        TRAINING_ROOM,
        CONFERENCE_ROOM,
        COMPUTER_LAB,
        AUDITORIUM,
        OUTDOOR_SPACE,
        PROJECTOR,
        LAPTOP,
        CAMERA,
        MICROPHONE,
        WHITEBOARD,
        VIDEO_CONFERENCING_KIT,
        TRAINER,
        INSTRUCTOR,
        MENTOR,
        EQUIPMENT_SET
    }

    public enum ResourceStatus {
        AVAILABLE,
        MAINTENANCE,
        RESERVED,
        UNAVAILABLE,
        RETIRED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Set<String> getFeatures() { return features; }
    public void setFeatures(Set<String> features) { this.features = features; }

    public Set<String> getIncludedEquipment() { return includedEquipment; }
    public void setIncludedEquipment(Set<String> includedEquipment) { this.includedEquipment = includedEquipment; }

    public Set<DayOfWeek> getAvailableDays() { return availableDays; }
    public void setAvailableDays(Set<DayOfWeek> availableDays) { this.availableDays = availableDays; }

    public LocalTime getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalTime availableFrom) { this.availableFrom = availableFrom; }

    public LocalTime getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(LocalTime availableUntil) { this.availableUntil = availableUntil; }

    public Integer getMinBookingMinutes() { return minBookingMinutes; }
    public void setMinBookingMinutes(Integer minBookingMinutes) { this.minBookingMinutes = minBookingMinutes; }

    public Integer getMaxBookingMinutes() { return maxBookingMinutes; }
    public void setMaxBookingMinutes(Integer maxBookingMinutes) { this.maxBookingMinutes = maxBookingMinutes; }

    public Integer getAdvanceBookingDays() { return advanceBookingDays; }
    public void setAdvanceBookingDays(Integer advanceBookingDays) { this.advanceBookingDays = advanceBookingDays; }

    public Integer getCancellationNoticeHours() { return cancellationNoticeHours; }
    public void setCancellationNoticeHours(Integer cancellationNoticeHours) { this.cancellationNoticeHours = cancellationNoticeHours; }

    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
