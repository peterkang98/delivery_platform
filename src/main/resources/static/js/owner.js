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
    window.location.href = "/view/owner/login";
}

// 메뉴 정의
const MENU_ITEMS = [
    { id: 'my-restaurant', label: '내 식당' },
    { id: 'orders', label: '주문정보' },
    { id: 'profile', label: '사용자 정보' }
];

let currentMenu = 'my-restaurant';

// 메인 레이아웃 렌더링
function renderMainLayout() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <div class="main-layout">
            <aside class="sidebar">
                <div class="user-info">
                    <h3>사장님</h3>
                    <p>owner@example.com</p>
                    <button class="logout-btn" onclick="logout()">로그아웃</button>
                </div>
                <nav class="nav-menu">
                    <ul id="menuList"></ul>
                </nav>
            </aside>
            <main class="main-content">
                <div class="content-header">
                    <h2 id="contentTitle">내 식당</h2>
                    <p id="contentDesc">식당 정보를 관리하세요</p>
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
        case 'my-restaurant':
            contentDesc.textContent = '식당 정보를 관리하세요';
            contentBody.innerHTML = `
                <h3>내 식당 관리</h3>
                <div style="margin-top: 20px;">
                    <p>식당 이름, 주소, 영업시간 등을 관리할 수 있습니다.</p>
                    <button class="btn btn-primary" style="margin-top: 20px;">
                        식당 정보 수정
                    </button>
                </div>
            `;
            break;

        case 'orders':
            contentDesc.textContent = '주문을 확인하고 관리하세요';
            contentBody.innerHTML = `
                <h3>주문 관리</h3>
                <div style="margin-top: 20px;">
                    <p>실시간 주문 현황을 확인하고 처리할 수 있습니다.</p>
                    <div style="margin-top: 20px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
                        <p style="color: #666;">현재 대기 중인 주문이 없습니다.</p>
                    </div>
                </div>
            `;
            break;

        case 'profile':
            contentDesc.textContent = '내 정보를 관리하세요';
            contentBody.innerHTML = `
                <h3>사용자 정보</h3>
                <div style="margin-top: 20px;">
                    <p>사장님 계정 정보를 관리할 수 있습니다.</p>
                    <button class="btn btn-primary" style="margin-top: 20px;">
                        정보 수정
                    </button>
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
    window.location.href = "/view/owner/login";
} else {
    renderMainLayout();
}