package com.freelms.reporting.entity;

import com.freelms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dashboard_widgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardWidget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false)
    private WidgetType widgetType;

    @Column(name = "data_source")
    private String dataSource;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config; // JSON

    @Column(name = "position_x")
    @Builder.Default
    private Integer positionX = 0;

    @Column(name = "position_y")
    @Builder.Default
    private Integer positionY = 0;

    @Column(name = "width")
    @Builder.Default
    private Integer width = 4;

    @Column(name = "height")
    @Builder.Default
    private Integer height = 3;

    @Column(name = "refresh_interval_seconds")
    private Integer refreshIntervalSeconds;

    public enum WidgetType {
        NUMBER_CARD,
        LINE_CHART,
        BAR_CHART,
        PIE_CHART,
        TABLE,
        HEATMAP,
        PROGRESS_BAR,
        LEADERBOARD,
        ACTIVITY_FEED
    }
}
