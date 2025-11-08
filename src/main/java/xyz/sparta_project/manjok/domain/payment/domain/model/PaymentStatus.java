package xyz.sparta_project.manjok.domain.payment.domain.model;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING("결제 대기"),
    APPROVED("결제 승인"),
    FAILED("결제 실패"),
    PARTIALLY_CANCELLED("부분 취소"),
    CANCELLED("전액 취소");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}