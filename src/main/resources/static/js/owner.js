function getToken() {
    return localStorage.getItem("authToken");
}
const app = document.getElementById("app");
const token = getToken();

if (!token) {
    app.innerHTML = `
        <div class="login-card">
            <h1>배달의 만족 OWNER</h1>
            <input type="text" placeholder="아이디" />
            <input type="password" placeholder="비밀번호" />
            <button>로그인</button>
        </div>
    `;
} else {
    app.innerHTML = `<h2>사장님 페이지 준비중...</h2>`;
}
