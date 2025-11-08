package xyz.sparta_project.manjok.global.common.dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import xyz.sparta_project.manjok.global.common.utils.UuidUtils;

import java.time.LocalDateTime;

/**
 * 기본 엔티티
 * - ID: UUID 36자 자동 생성
 * - createdAt: 서버 현재 시간 자동 설정
 * 사용 예시
 * <pre>
 *     {@code
 *      @Entity
 *      public class Entity extends BaseEntity {
 *          private String data;
 *          //ID와 CreatedAt는 자동 관리
 *      }
 *     }
 * </pre>
 * */
@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * UUID 기반 ID (36자)
     * */
    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id = UuidUtils.generate();

    /**
     * 생성 시간 (서버 시간 기준)
     * */
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * UUID 재생성 전략
     * */
    public void regenerateId() {
        this.id = UuidUtils.generate();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if(!(obj instanceof BaseEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
