package xyz.sparta_project.manjok.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionRequestStatus {
	PENDING("승급 요청 대기 중"),
	ACCEPTED("승급 요청 승인됨"),
	REJECTED("승급 요청 거절됨");

	private final String description;
}
