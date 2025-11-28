package com.freelms.booking.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resource booking/reservation entity.
 */
@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_resource_time", columnList = "resource_id, start_time, end_time"),
    @Index(name = "idx_booking_user", columnList = "booked_by"),
    @Index(name = "idx_booking_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    // Booking details
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String purpose;

    private Integer expectedAttendees;

    // Related entities
    private Long courseId;
    private Long eventId;
    private Long sessionId;

    // Users
    @Column(name = "booked_by", nullable = false)
    private Long bookedBy;

    private Long approvedBy;
    private LocalDateTime approvedAt;

    private Long cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Recurrence
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    private RecurrencePattern recurrencePattern;

    private Integer recurrenceInterval;
    private LocalDateTime recurrenceEndDate;
    private UUID parentBookingId;

    // Setup/teardown time
    private Integer setupMinutes = 0;
    private Integer teardownMinutes = 0;

    // Equipment requests
    @Column(length = 2000)
    private String equipmentRequests;

    // Check-in/out
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private Long checkedInBy;

    // Notes
    @Column(length = 2000)
    private String internalNotes;

    @Column(length = 2000)
    private String specialRequirements;

    // Cost tracking
    private Double totalCost;
    private String costCenter;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum BookingStatus {
        PENDING,
        APPROVED,
        CONFIRMED,
        CHECKED_IN,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        REJECTED
    }

    public enum RecurrencePattern {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        CUSTOM
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public Integer getExpectedAttendees() { return expectedAttendees; }
    public void setExpectedAttendees(Integer expectedAttendees) { this.expectedAttendees = expectedAttendees; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getBookedBy() { return bookedBy; }
    public void setBookedBy(Long bookedBy) { this.bookedBy = bookedBy; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public Long getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Long cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean recurring) { isRecurring = recurring; }

    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) { this.recurrencePattern = recurrencePattern; }

    public Integer getRecurrenceInterval() { return recurrenceInterval; }
    public void setRecurrenceInterval(Integer recurrenceInterval) { this.recurrenceInterval = recurrenceInterval; }

    public LocalDateTime getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDateTime recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public UUID getParentBookingId() { return parentBookingId; }
    public void setParentBookingId(UUID parentBookingId) { this.parentBookingId = parentBookingId; }

    public Integer getSetupMinutes() { return setupMinutes; }
    public void setSetupMinutes(Integer setupMinutes) { this.setupMinutes = setupMinutes; }

    public Integer getTeardownMinutes() { return teardownMinutes; }
    public void setTeardownMinutes(Integer teardownMinutes) { this.teardownMinutes = teardownMinutes; }

    public String getEquipmentRequests() { return equipmentRequests; }
    public void setEquipmentRequests(String equipmentRequests) { this.equipmentRequests = equipmentRequests; }

    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }

    public LocalDateTime getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(LocalDateTime checkedOutAt) { this.checkedOutAt = checkedOutAt; }

    public Long getCheckedInBy() { return checkedInBy; }
    public void setCheckedInBy(Long checkedInBy) { this.checkedInBy = checkedInBy; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
