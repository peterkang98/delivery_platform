const API_BASE_URL = "/v1/auth";

// URL 경로에서 역할 타입 추출
function getRoleTypeFromPath() {
    const path = window.location.pathname;
    if (path.includes('/client')) return 'CLIENT';
    if (path.includes('/owner')) return 'OWNER';
    if (path.includes('/admin')) return 'ADMIN';
    return null;
}

// 토큰 키 반환
function getTokenKey() {
    return `authToken_${getRoleTypeFromPath()}`;
}

// 토큰 관리
function getToken() {
    return localStorage.getItem(getTokenKey());
}

function setToken(token) {
    localStorage.setItem(getTokenKey(), token);
}

function removeToken() {
    localStorage.removeItem(getTokenKey());
}

// 로그인 API
async function login(email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) throw new Error("로그인 실패");

        const result = await response.json();
        setToken(result.data);
        window.location.href = "/view/client";
        return true;
    } catch (error) {
        console.error("로그인 오류:", error);
        alert("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        return false;
    }
}

// 회원가입 API
async function signup(username, password, confirmPassword, email) {
    try {
        const response = await fetch(`${API_BASE_URL}/signup`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password, confirmPassword, email })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || "회원가입 실패");
        }

        const result = await response.json();
        return { success: true, data: result };
    } catch (error) {
        console.error("회원가입 오류:", error);
        return { success: false, message: error.message };
    }
}

function validateData() {

}

// 로그인 화면 렌더링
function renderLoginPage() {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>로그인</h2>
            </div>
            <div class="auth-form">
                <div class="input-with-button">
                    <div class="input-group">
                        <input type="text" id="email" placeholder="이메일" value="testClient@test.com" />
                        <input type="password" id="password" placeholder="비밀번호" value="Qwer1234!" />
                    </div>
                    <button class="btn-login" id="loginBtn">로그인</button>
                </div>
                <div class="auth-links">
                    <a id="signupLink">회원가입</a>
                    <span class="divider">|</span>
                    <a id="findIdLink">아이디 찾기</a>
                    <span class="divider">|</span>
                    <a id="findPwLink">비밀번호 찾기</a>
                </div>
            </div>
        </div>
    `;

    // 로그인 버튼 이벤트
    document.getElementById("loginBtn").addEventListener("click", () => {
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!email || !password) {
            alert("이메일과 비밀번호를 입력해주세요.");
            return;
        }
        login(email, password);
    });

    // Enter 키 이벤트
    document.getElementById("password").addEventListener("keypress", (e) => {
        if (e.key === "Enter") document.getElementById("loginBtn").click();
    });

    // 회원가입 링크
    document.getElementById("signupLink").addEventListener("click", renderSignupPage);

    // 아이디/비밀번호 찾기 (임시)
    document.getElementById("findIdLink").addEventListener("click", () => {
        alert("아이디 찾기 기능은 준비중입니다.");
    });
    document.getElementById("findPwLink").addEventListener("click", () => {
        alert("비밀번호 찾기 기능은 준비중입니다.");
    });
}

// 회원가입 화면 렌더링
function renderSignupPage() {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>회원가입</h2>
            </div>
            <div class="auth-form">
                <div class="form-group">
                    <label>아이디</label>
                    <input type="text" id="username" placeholder="4~10자 입력" />
                    <div class="error-message" id="usernameError"></div>
                </div>
                <div class="form-group">
                    <label>이메일</label>
                    <input type="email" id="email" placeholder="example@email.com" />
                    <div class="error-message" id="emailError"></div>
                </div>
                <div class="form-group">
                    <label>비밀번호</label>
                    <input type="password" id="password" placeholder="8~15자 입력" />
                    <div class="error-message" id="passwordError"></div>
                </div>
                <div class="form-group">
                    <label>비밀번호 확인</label>
                    <input type="password" id="confirmPassword" placeholder="비밀번호 재입력" />
                    <div class="error-message" id="confirmPasswordError"></div>
                </div>
                <button class="btn-signup-main" id="signupBtn">회원가입</button>
                <div class="auth-links">
                    <a id="backToLogin">로그인으로 돌아가기</a>
                </div>
            </div>
        </div>
    `;

    // 회원가입 버튼 이벤트
    document.getElementById("signupBtn").addEventListener("click", async () => {
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();

        // 에러 메시지 초기화
        document.querySelectorAll(".error-message").forEach(el => {
            el.textContent = "";
            el.classList.remove("show");
        });

        // 유효성 검사
        let hasError = false;

        if (!username || username.length < 4 || username.length > 10) {
            document.getElementById("usernameError").textContent = "아이디는 4~10자여야 합니다.";
            document.getElementById("usernameError").classList.add("show");
            hasError = true;
        }

        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            document.getElementById("emailError").textContent = "올바른 이메일을 입력해주세요.";
            document.getElementById("emailError").classList.add("show");
            hasError = true;
        }

        if (!password || password.length < 8 || password.length > 15) {
            document.getElementById("passwordError").textContent = "비밀번호는 8~15자여야 합니다.";
            document.getElementById("passwordError").classList.add("show");
            hasError = true;
        }

        if (password !== confirmPassword) {
            document.getElementById("confirmPasswordError").textContent = "비밀번호가 일치하지 않습니다.";
            document.getElementById("confirmPasswordError").classList.add("show");
            hasError = true;
        }

        if (hasError) return;

        // 회원가입 API 호출
        const result = await signup(username, password, confirmPassword, email);

        if (result.success) {
            renderSignupSuccess(email);
        } else {
            alert(result.message || "회원가입에 실패했습니다.");
        }
    });

    // 로그인으로 돌아가기
    document.getElementById("backToLogin").addEventListener("click", renderLoginPage);
}

// 회원가입 성공 화면
function renderSignupSuccess(email) {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>회원가입 완료</h2>
            </div>
            <div class="auth-form">
                <div class="success-message">
                    <h3>🎉 회원가입이 완료되었습니다!</h3>
                    <p>
                        <strong>${email}</strong>로<br/>
                        인증 이메일이 발송되었습니다.<br/><br/>
                        이메일에서 인증 버튼을 누른 후<br/>
                        로그인해주세요!
                    </p>
                    <button class="btn-back-login" id="toLoginBtn">로그인 하기</button>
                </div>
            </div>
        </div>
    `;

    document.getElementById("toLoginBtn").addEventListener("click", renderLoginPage);
}

// 페이지 로드 시
const token = getToken();
if (token) {
    window.location.href = "/view/client";
} else {
    renderLoginPage();
}