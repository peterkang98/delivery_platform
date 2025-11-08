const API_BASE_URL = "/v1/auth";

// URL에서 토큰 추출
function getTokenFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('token');
}

// 비밀번호 재설정 API 호출
async function resetPassword(token, newPassword, confirmPassword) {
    try {
        const response = await fetch(`${API_BASE_URL}/confirm-password-reset`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token, newPassword, confirmPassword })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || "비밀번호 재설정에 실패했습니다.");
        }

        return { success: true, data: result };
    } catch (error) {
        console.error("비밀번호 재설정 오류:", error);
        return { success: false, message: error.message };
    }
}

// 비밀번호 유효성 검사
function validatePassword(password) {
    if (!password || password.length < 8 || password.length > 15) {
        return "비밀번호는 8~15자여야 합니다.";
    }

    // 영문, 숫자, 특수문자 포함 검사
    const hasLetter = /[a-zA-Z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    if (!hasLetter || !hasNumber || !hasSpecial) {
        return "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.";
    }

    return null;
}

// 비밀번호 재설정 화면 렌더링
function renderResetPasswordPage() {
    const app = document.getElementById("app");
    const token = getTokenFromUrl();

    if (!token) {
        app.innerHTML = `
            <div class="auth-card">
                <div class="auth-header">
                    <h1>배달의 만족</h1>
                    <h2>비밀번호 재설정</h2>
                </div>
                <div class="auth-form">
                    <div class="error-box">
                        <p>⚠️ 유효하지 않은 접근입니다.</p>
                        <p>비밀번호 재설정 링크를 다시 확인해주세요.</p>
                    </div>
                    <button class="btn-close" onclick="window.close()">창 닫기</button>
                </div>
            </div>
        `;
        return;
    }

    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>비밀번호 재설정</h2>
            </div>
            <div class="auth-form">
                <div class="form-group">
                    <label>새 비밀번호</label>
                    <input type="password" id="newPassword" placeholder="8~15자 (영문, 숫자, 특수문자 포함)" />
                    <div class="error-message" id="passwordError"></div>
                </div>
                <div class="form-group">
                    <label>비밀번호 확인</label>
                    <input type="password" id="confirmPassword" placeholder="비밀번호 재입력" />
                    <div class="error-message" id="confirmPasswordError"></div>
                </div>
                <button class="btn-reset" id="resetBtn">비밀번호 변경</button>
            </div>
        </div>
    `;

    // 비밀번호 변경 버튼 이벤트
    document.getElementById("resetBtn").addEventListener("click", async () => {
        const newPassword = document.getElementById("newPassword").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();

        // 에러 메시지 초기화
        document.querySelectorAll(".error-message").forEach(el => {
            el.textContent = "";
            el.classList.remove("show");
        });

        // 유효성 검사
        let hasError = false;

        const passwordError = validatePassword(newPassword);
        if (passwordError) {
            document.getElementById("passwordError").textContent = passwordError;
            document.getElementById("passwordError").classList.add("show");
            hasError = true;
        }

        if (newPassword !== confirmPassword) {
            document.getElementById("confirmPasswordError").textContent = "비밀번호가 일치하지 않습니다.";
            document.getElementById("confirmPasswordError").classList.add("show");
            hasError = true;
        }

        if (hasError) return;

        // 비밀번호 재설정 API 호출
        const result = await resetPassword(token, newPassword, confirmPassword);

        if (result.success) {
            renderResetSuccess();
        } else {
            alert(result.message || "비밀번호 재설정에 실패했습니다.");
        }
    });

    // Enter 키 이벤트
    document.getElementById("confirmPassword").addEventListener("keypress", (e) => {
        if (e.key === "Enter") document.getElementById("resetBtn").click();
    });
}

// 비밀번호 재설정 성공 화면
function renderResetSuccess() {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>비밀번호 재설정 완료</h2>
            </div>
            <div class="auth-form">
                <div class="success-message">
                    <h3>✅ 비밀번호가 성공적으로 변경되었습니다!</h3>
                    <p>
                        새 비밀번호로 로그인해주세요.<br/><br/>
                        이 창을 닫고 로그인 페이지로<br/>
                        돌아가주세요.
                    </p>
                    <button class="btn-close" onclick="window.close()">창 닫기</button>
                </div>
            </div>
        </div>
    `;
}

// 페이지 로드 시 실행
document.addEventListener("DOMContentLoaded", () => {
    renderResetPasswordPage();
});