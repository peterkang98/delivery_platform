package xyz.sparta_project.manjok.domain.payment.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import xyz.sparta_project.manjok.domain.payment.domain.client.PaymentClient;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;
import xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto.TossPaymentResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 API 클라이언트 구현체
 * https://docs.tosspayments.com/reference
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentClient {

    private final RestTemplate geminiRestTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Value("${toss.api.url:https://api.tosspayments.com}")
    private String tossApiUrl;

    private static final String PAYMENTS_PATH = "/v1/payments";

    /**
     * 결제 정보 조회
     * GET /v1/payments/{paymentKey}
     */
    @Override
    public TossPaymentResponse getPayment(String paymentKey) {
        String url = tossApiUrl + PAYMENTS_PATH + "/" + paymentKey;

        log.info("토스 결제 조회 API 호출 - paymentKey: {}", paymentKey);

        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<TossPaymentResponse> response = geminiRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    TossPaymentResponse.class
            );

            log.info("토스 결제 조회 성공 - paymentKey: {}, status: {}",
                    paymentKey, response.getBody().getStatus());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("토스 결제 조회 실패 - paymentKey: {}, status: {}, response: {}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "토스 결제 조회에 실패했습니다: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("토스 결제 조회 중 예외 발생 - paymentKey: {}", paymentKey, e);
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "토스 결제 조회 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 결제 승인
     * POST /v1/payments/confirm
     */
    @Override
    public TossPaymentResponse approvePayment(String paymentKey, String orderId, String amount) {
        String url = tossApiUrl + PAYMENTS_PATH + "/confirm";

        log.info("토스 결제 승인 API 호출 - paymentKey: {}, orderId: {}, amount: {}",
                paymentKey, orderId, amount);

        try {
            HttpHeaders headers = createHeaders();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("paymentKey", paymentKey);
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<TossPaymentResponse> response = geminiRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );

            log.info("토스 결제 승인 성공 - paymentKey: {}", paymentKey);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("토스 결제 승인 실패 - paymentKey: {}, status: {}, response: {}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "토스 결제 승인에 실패했습니다: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("토스 결제 승인 중 예외 발생 - paymentKey: {}", paymentKey, e);
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "토스 결제 승인 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 결제 취소
     * POST /v1/payments/{paymentKey}/cancel
     */
    @Override
    public TossPaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        String url = tossApiUrl + PAYMENTS_PATH + "/" + paymentKey + "/cancel";

        log.info("토스 결제 취소 API 호출 - paymentKey: {}, reason: {}", paymentKey, cancelReason);

        try {
            HttpHeaders headers = createHeaders();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<TossPaymentResponse> response = geminiRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );

            log.info("토스 결제 취소 성공 - paymentKey: {}", paymentKey);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("토스 결제 취소 실패 - paymentKey: {}, status: {}, response: {}",
                    paymentKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_CANCEL_FAILED,
                    "토스 결제 취소에 실패했습니다: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("토스 결제 취소 중 예외 발생 - paymentKey: {}", paymentKey, e);
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_CANCEL_FAILED,
                    "토스 결제 취소 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * HTTP 헤더 생성
     * Authorization: Basic {Base64(secretKey:)}
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 토스 API는 시크릿 키를 Base64 인코딩하여 Basic Auth로 사용
        String encodedAuth = Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        return headers;
    }
}