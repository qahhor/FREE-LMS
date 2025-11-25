package com.freelms.organization.entity;

import com.freelms.common.entity.BaseEntity;
import com.freelms.common.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organizations_slug", columnList = "slug", unique = true),
    @Index(name = "idx_organizations_owner", columnList = "owner_id")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Organization extends BaseEntity {
    @Column(nullable = false, length = 255) private String name;
    @Column(nullable = false, unique = true, length = 150) private String slug;
    @Column(length = 1000) private String description;
    @Column(name = "logo_url", length = 500) private String logoUrl;
    @Column(name = "primary_color", length = 7) private String primaryColor;
    @Column(name = "owner_id", nullable = false) private Long ownerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private SubscriptionTier tier = SubscriptionTier.FREE;
    @Column(name = "is_active") @Builder.Default private boolean isActive = true;
    @Column(name = "max_members") @Builder.Default private Integer maxMembers = 5;
    @Column(name = "custom_domain", length = 255) private String customDomain;
}
