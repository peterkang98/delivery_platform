// 페이지 로드 시 URL 파라미터 확인
const urlParams = new URLSearchParams(window.location.search);

function getToken() {
    return localStorage.getItem("authToken");
}

const app = document.getElementById("app");
const token = getToken();

if (!token) {
    app.innerHTML = `
        <div class="login-card">
            <h1>배달의 만족</h1>
            <input id="login-id" type="text" placeholder="아이디" />
            <input id="login-pw" type="password" placeholder="비밀번호" />
            <button id="login-btn">로그인</button>
        </div>
    `;
    document.getElementById("login-btn").addEventListener("click", () => {
        localStorage.setItem("authToken", "TEST-TOKEN");
        location.reload();
    });

} else {
    app.innerHTML = `
        <div class="layout">
            <aside class="sidebar expanded">
                <div class="toggle">☰</div>
                <ul>
                    <li>홈</li>
                    <li>주문 내역</li>
                    <li>찜 목록</li>
                </ul>
            </aside>
            <header class="header"></header>
            <main class="content">
                <h2>결제 테스트</h2>
                <button id="pay-button">결제하기</button>

                <!-- ✅ 결제 결과 표시 영역 -->
                <div id="result-area" style="margin-top: 30px;"></div>
            </main>
        </div>
    `;

    // ✅ 결제 결과 확인 및 표시
    handlePaymentCallback();

    setupPayment();
}

// ✅ 결제 콜백 처리 함수
function handlePaymentCallback() {
    const paymentKey = urlParams.get('paymentKey');
    const orderId = urlParams.get('orderId');
    const amount = urlParams.get('amount');
    const code = urlParams.get('code');
    const message = urlParams.get('message');

    if (paymentKey && orderId) {
        // 결제 성공
        displayPaymentResult({
            paymentKey: paymentKey,
            orderId: orderId,
            amount: amount,
            paymentType: "CARD"
        });

        // ✅ URL 파라미터 제거 (선택사항)
        // window.history.replaceState({}, document.title, window.location.pathname);

    } else if (code && message) {
        // 결제 실패
        displayPaymentFail({
            code: code,
            message: message
        });

        // ✅ URL 파라미터 제거 (선택사항)
        // window.history.replaceState({}, document.title, window.location.pathname);
    }
}

async function setupPayment() {
    const clientKey = "test_ck_yZqmkKeP8g4baNqxOKLp3bQRxB9l";
    const tossPayments = await TossPayments(clientKey);

    let customerKey = localStorage.getItem("customerKey");
    if (!customerKey) {
        customerKey = crypto.randomUUID();
        localStorage.setItem("customerKey", customerKey);
    }

    const payment = tossPayments.payment({ customerKey });

    document.getElementById("pay-button").addEventListener("click", async () => {
        try {
            await payment.requestPayment({
                method: "CARD",
                amount: {
                    currency: "KRW",
                    value: 100
                },
                orderId: generateOrderId(),
                orderName: "테스트 결제",
                customerName: "사용자",
                // 현재 페이지로 리다이렉트 (파라미터로 결과 전달)
                successUrl: `${window.location.origin}${window.location.pathname}`,
                failUrl: `${window.location.origin}${window.location.pathname}`,
            });
        } catch (error) {
            console.error("결제 오류:", error);
            displayPaymentFail(error);
        }
    });
}

function displayPaymentResult(result) {
    document.getElementById("result-area").innerHTML = `
        <div style="border:1px solid #0a0; padding:20px; border-radius:6px; background:#f6fff6;">
            <h3>✅ 결제 완료</h3>
            <p><b>결제 수단:</b> ${result.paymentType}</p>
            <p><b>결제 금액:</b> ${result.amount}원</p>
            <p><b>주문번호:</b> ${result.orderId}</p>
            <p><b>paymentKey:</b> ${result.paymentKey}</p>
            <small style="color:#888;">(웹훅 수신 후 백엔드 저장 예정)</small>
        </div>
    `;
}

function displayPaymentFail(error) {
    document.getElementById("result-area").innerHTML = `
        <div style="border:1px solid #a00; padding:20px; border-radius:6px; background:#fff6f6;">
            <h3>❌ 결제 실패</h3>
            <p><b>코드:</b> ${error.code}</p>
            <p><b>메시지:</b> ${error.message}</p>
            <small style="color:#888;">(웹훅 및 승인 처리 없음)</small>
        </div>
    `;
}

function generateOrderId() {
    return "order_" + Math.random().toString(36).slice(2, 11);
}