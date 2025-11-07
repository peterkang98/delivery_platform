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
        window.location.href = "/view/owner";
        return true;
    } catch (error) {
        console.error("로그인 오류:", error);
        alert("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        return false;
    }
}

// 로그인 화면 렌더링
function renderLoginPage() {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="auth-card">
            <div class="auth-header">
                <h1>배달의 만족</h1>
                <h2>사장님 로그인</h2>
            </div>
            <div class="auth-form">
                <div class="input-with-button">
                    <div class="input-group">
                        <input type="text" id="email" placeholder="이메일" value="testOwner1@test.com" />
                        <input type="password" id="password" placeholder="비밀번호" value="Qwer1234!" />
                    </div>
                    <button class="btn-login" id="loginBtn">로그인</button>
                </div>
                <div class="auth-links">
                    <span style="color: #666; font-style: italic;">
                        * 사장님 계정은 고객 계정에서 승급 신청 후 이용 가능합니다.
                    </span>
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
}

// 페이지 로드 시
const token = getToken();
if (token) {
    window.location.href = "/view/owner";
} else {
    renderLoginPage();
}