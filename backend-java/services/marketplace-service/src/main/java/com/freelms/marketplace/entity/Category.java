package com.freelms.marketplace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Smartup LMS - Marketplace Category Entity
 */
@Entity
@Table(name = "marketplace_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_uz")
    private String nameUz;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon")
    private String icon; // Icon class or URL

    @Column(name = "color")
    private String color; // Hex color

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Category> children = new HashSet<>();

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "item_count")
    private Long itemCount = 0L;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "featured")
    private boolean featured;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public boolean isRoot() {
        return parent == null;
    }

    public void incrementItemCount() {
        this.itemCount++;
    }

    public void decrementItemCount() {
        if (this.itemCount > 0) {
            this.itemCount--;
        }
    }
}
