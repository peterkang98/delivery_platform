package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.DayType;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingDay;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.OperatingTimeType;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * OperatingDay JPA Entity
 * - Restaurant에 종속됨 (영속성 전이)
 * - 복합키 사용 (restaurant + dayType + timeType)
 * - BaseEntity를 상속받지 않음 (Value Object 성격)
 */
@Entity
@Table(name = "p_operating_days", indexes = {
        @Index(name = "idx_operating_day_restaurant_id", columnList = "restaurant_id")
})
@IdClass(OperatingDayEntity.OperatingDayId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OperatingDayEntity {

    // Restaurant와의 관계
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private RestaurantEntity restaurant;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", length = 20, nullable = false)
    private DayType dayType;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "time_type", length = 50, nullable = false)
    private OperatingTimeType timeType;

    // 운영 시간
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_holiday", nullable = false)
    @Builder.Default
    private Boolean isHoliday = false;

    // 브레이크 타임
    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    // 특이사항
    @Column(name = "note", length = 500)
    private String note;

    // ==================== 도메인 ↔ 엔티티 변환 ====================

    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static OperatingDayEntity fromDomain(OperatingDay domain) {
        if (domain == null) {
            return null;
        }

        return OperatingDayEntity.builder()
                .dayType(domain.getDayType())
                .timeType(domain.getTimeType())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .isHoliday(domain.getIsHoliday())
                .breakStartTime(domain.getBreakStartTime())
                .breakEndTime(domain.getBreakEndTime())
                .note(domain.getNote())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    public OperatingDay toDomain() {
        return OperatingDay.builder()
                .restaurantId(this.restaurant != null ? this.restaurant.getId() : null)
                .dayType(this.dayType)
                .timeType(this.timeType)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .isHoliday(this.isHoliday)
                .breakStartTime(this.breakStartTime)
                .breakEndTime(this.breakEndTime)
                .note(this.note)
                .build();
    }

    // ==================== Helper Methods ====================

    /**
     * Restaurant ID 조회 (복합키용)
     */
    public String getRestaurantId() {
        return this.restaurant != null ? this.restaurant.getId() : null;
    }

    // ==================== 복합키 클래스 ====================

    /**
     * 복합키 클래스
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class OperatingDayId implements Serializable {
        private String restaurant;  // RestaurantEntity의 id 필드에 매핑
        private DayType dayType;
        private OperatingTimeType timeType;
    }
}