package xyz.sparta_project.manjok.domain.payment.domain.model;

/**
 * 결제 취소 유형
 */
public enum CancellationType {
    USER_REQUEST("사용자 요청"),
    SYSTEM_ERROR("시스템 오류"),
    ADMIN_CANCEL("관리자 취소"),
    FRAUD_DETECTION("부정 거래 감지"),
    TIMEOUT("타임아웃");

    private final String description;

    CancellationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}