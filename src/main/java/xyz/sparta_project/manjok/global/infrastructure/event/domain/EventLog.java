package xyz.sparta_project.manjok.global.infrastructure.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

/**
 * 이벤트 로그 엔티티
 * - 이벤트 이름, 페이로드, 상태, 재시도 횟수, 수정 시간 관리
 * - BaseEntity 상속: id(UUID), createdAt 자동 관리
 * */
@Entity
@Table(name = "p_event_log")
@Getter
public class EventLog extends BaseEntity {

    /**
     * 이벤트 이름
     * */
    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    /**
     * 이벤트 페이로드 (JSON 등)
     * */
    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    /**
     * 이벤트 상태 (PENDING, SUCCESS, FAILED, RETRYING 등)
     * */
    private EventStatus status = EventStatus.PENDING;
    
    /**
     * 재시도 횟수
     * */
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;
    
    /**
     * 수정 시간
     * */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * 기본 생성자
     * */
    protected EventLog() {
    }
    
    /**
     * 생성 팩토리 메서드
     * */
    public static EventLog of(String eventName, String payload) {
        if (eventName == null || eventName.isBlank()) {
            throw new IllegalArgumentException("이벤트 이름은 필수 입력 값입니다.");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("이벤트 페이로드는 필수 입력 값입니다.");
        }
        return new EventLog(eventName, payload);
    }

    /**
     * 생성자 (팩토리 내부 전용)
     * */
    private EventLog(String eventName,String payload) {
        this.eventName = eventName;
        this.payload = payload;
        this.status = EventStatus.PENDING;
        this.retryCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상태 업데이트
     * */
    public void updateStatus(EventStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 횟수 증가
     * */
    public void increaseRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 횟수 직접 설정 (음수 불가)
     * */
    public void setRetryCount(int retryCount) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("재시도 횟수는 음수가 될 수 없습니다.");
        }
        this.retryCount = retryCount;
        this.updatedAt = LocalDateTime.now();
    }

}
