package xyz.sparta_project.manjok.global.infrastructure.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;

import java.util.List;

/**
 * EventLogRepository
 * - 이벤트 로그의 저장 및 상태별 조회를 담당하는 JPA 리포지토리
 * */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, String> {
    /**
     * 상태별 이벤트 로그 조회
     * @param status 조회할 이벤트 상태
     * @return 해당 상태의 이벤트 로그 리스트
     * */
    List<EventLog> findAllByStatus(EventStatus status);
}
