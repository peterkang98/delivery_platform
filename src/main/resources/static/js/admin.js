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

function removeToken() {
    localStorage.removeItem(getTokenKey());
}

function logout() {
    removeToken();
    window.location.href = "/view/admin/login";
}

// 메뉴 정의
const MENU_ITEMS = [
    { id: 'restaurants', label: '식당정보' },
    { id: 'clients', label: '클라이언트정보' },
    { id: 'owners', label: '오너정보' },
    { id: 'statistics', label: '통계정보' }
];

let currentMenu = 'restaurants';

// 메인 레이아웃 렌더링
function renderMainLayout() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <div class="main-layout">
            <aside class="sidebar">
                <div class="user-info">
                    <h3>관리자</h3>
                    <p>admin@example.com</p>
                    <button class="logout-btn" onclick="logout()">로그아웃</button>
                </div>
                <nav class="nav-menu">
                    <ul id="menuList"></ul>
                </nav>
            </aside>
            <main class="main-content">
                <div class="content-header">
                    <h2 id="contentTitle">식당정보</h2>
                    <p id="contentDesc">전체 식당 정보를 관리하세요</p>
                </div>
                <div class="content-body" id="contentBody">
                    <!-- 여기에 콘텐츠가 렌더링됩니다 -->
                </div>
            </main>
        </div>
    `;

    renderMenu();
    renderContent(currentMenu);
}

// 메뉴 렌더링
function renderMenu() {
    const menuList = document.getElementById('menuList');
    menuList.innerHTML = MENU_ITEMS.map(item => `
        <li class="${currentMenu === item.id ? 'active' : ''}" onclick="changeMenu('${item.id}')">
            ${item.label}
        </li>
    `).join('');
}

// 메뉴 변경
function changeMenu(menuId) {
    currentMenu = menuId;
    renderMenu();
    renderContent(menuId);
}

// 콘텐츠 렌더링
function renderContent(menuId) {
    const contentTitle = document.getElementById('contentTitle');
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    const menuItem = MENU_ITEMS.find(item => item.id === menuId);
    contentTitle.textContent = menuItem.label;

    switch(menuId) {
        case 'restaurants':
            contentDesc.textContent = '전체 식당 정보를 관리하세요';
            contentBody.innerHTML = `
                <h3>식당 관리</h3>
                <div style="margin-top: 20px;">
                    <p>등록된 모든 식당의 정보를 조회하고 관리할 수 있습니다.</p>
                    <div style="margin-top: 20px;">
                        <button class="btn btn-primary">식당 목록 조회</button>
                        <button class="btn btn-success" style="margin-left: 10px;">식당 승인 대기 목록</button>
                    </div>
                </div>
            `;
            break;

        case 'clients':
            contentDesc.textContent = '전체 고객 정보를 관리하세요';
            contentBody.innerHTML = `
                <h3>클라이언트 관리</h3>
                <div style="margin-top: 20px;">
                    <p>등록된 모든 고객의 정보를 조회하고 관리할 수 있습니다.</p>
                    <div style="margin-top: 20px;">
                        <button class="btn btn-primary">고객 목록 조회</button>
                        <button class="btn btn-danger" style="margin-left: 10px;">정지된 계정 관리</button>
                    </div>
                </div>
            `;
            break;

        case 'owners':
            contentDesc.textContent = '전체 사장님 정보를 관리하세요';
            contentBody.innerHTML = `
                <h3>오너 관리</h3>
                <div style="margin-top: 20px;">
                    <p>등록된 모든 사장님의 정보를 조회하고 관리할 수 있습니다.</p>
                    <div style="margin-top: 20px;">
                        <button class="btn btn-primary">오너 목록 조회</button>
                        <button class="btn btn-success" style="margin-left: 10px;">승급 대기 목록</button>
                    </div>
                </div>
            `;
            break;

        case 'statistics':
            contentDesc.textContent = '서비스 통계를 확인하세요';
            contentBody.innerHTML = `
                <h3>통계 정보</h3>
                <div style="margin-top: 20px;">
                    <p>서비스 이용 통계와 매출 정보를 확인할 수 있습니다.</p>
                    <div style="margin-top: 20px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px;">
                        <div style="padding: 20px; background: #f0f9ff; border-radius: 8px; border: 1px solid #bae6fd;">
                            <h4 style="color: #0369a1; margin-bottom: 10px;">총 주문 수</h4>
                            <p style="font-size: 24px; font-weight: bold; color: #0369a1;">0건</p>
                        </div>
                        <div style="padding: 20px; background: #f0fdf4; border-radius: 8px; border: 1px solid #bbf7d0;">
                            <h4 style="color: #15803d; margin-bottom: 10px;">총 매출</h4>
                            <p style="font-size: 24px; font-weight: bold; color: #15803d;">0원</p>
                        </div>
                        <div style="padding: 20px; background: #fef3c7; border-radius: 8px; border: 1px solid #fde68a;">
                            <h4 style="color: #92400e; margin-bottom: 10px;">등록 식당 수</h4>
                            <p style="font-size: 24px; font-weight: bold; color: #92400e;">0개</p>
                        </div>
                        <div style="padding: 20px; background: #fce7f3; border-radius: 8px; border: 1px solid #fbcfe8;">
                            <h4 style="color: #9f1239; margin-bottom: 10px;">가입 회원 수</h4>
                            <p style="font-size: 24px; font-weight: bold; color: #9f1239;">0명</p>
                        </div>
                    </div>
                </div>
            `;
            break;

        default:
            contentBody.innerHTML = `<p>준비 중입니다...</p>`;
    }
}

// 페이지 로드
const token = getToken();

if (!token) {
    window.location.href = "/view/admin/login";
} else {
    renderMainLayout();
}