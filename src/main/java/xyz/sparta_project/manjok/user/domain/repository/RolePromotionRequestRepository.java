package xyz.sparta_project.manjok.user.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.user.domain.entity.RolePromotionRequest;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.vo.PromotionRequestStatus;

import java.util.List;

public interface RolePromotionRequestRepository extends JpaRepository<RolePromotionRequest, String> {
	List<RolePromotionRequest> findByRequester(User requester);

	@EntityGraph(attributePaths = {"requester", "reviewer"})
	Page<RolePromotionRequest> findAllByStatus(PromotionRequestStatus status, Pageable pageable);
}
