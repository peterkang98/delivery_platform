package xyz.sparta_project.manjok.domain.payment.domain.model;

/**
 * 결제 수단
 */
public enum PaymentMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    TRANSFER("계좌이체"),
    MOBILE("휴대폰"),
    EASY_PAY("간편결제");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}