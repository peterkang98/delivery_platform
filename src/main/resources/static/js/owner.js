// ==================== 유틸리티 함수 ====================

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

// API 호출 헬퍼
async function apiCall(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || '요청 실패');
        }

        return data;
    } catch (error) {
        console.error('API 호출 오류:', error);
        showNotification(error.message || '오류가 발생했습니다.', 'error');
        throw error;
    }
}

// 알림 표시
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// ==================== 전역 상태 ====================

let currentMenu = 'my-restaurant';
let currentUser = null;
let currentRestaurants = [];
let currentMenus = [];
let categories = [];
let selectedRestaurantId = null;

// ==================== 메뉴 정의 ====================

const MENU_ITEMS = [
    { id: 'my-restaurant', label: '내 식당' },
    { id: 'payments', label: '결제 정보' },
    { id: 'profile', label: '사용자 정보' }
];

// ==================== API 함수 ====================

// 사용자 정보 조회
async function fetchUserInfo() {
    try {
        const response = await apiCall('/v1/users');
        if (response && response.data) {
            currentUser = response.data;
            return response.data;
        }
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
    }
    return null;
}

// 카테고리 목록 조회
async function fetchCategories() {
    try {
        const response = await apiCall('/v1/common/restaurants/categories');
        if (response && response.data) {
            categories = response.data;
            return response.data;
        }
    } catch (error) {
        console.error('카테고리 조회 실패:', error);
    }
    return [];
}

// 레스토랑 목록 조회
async function fetchMyRestaurants() {
    try {
        const response = await apiCall('/v1/owners/restaurants');
        if (response && response.data) {
            currentRestaurants = response.data.content || [];
            return currentRestaurants;
        }
    } catch (error) {
        console.error('레스토랑 목록 조회 실패:', error);
    }
    return [];
}

// 레스토랑 상세 조회
async function fetchRestaurantDetail(restaurantId) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}`);
        if (response && response.data) {
            return response.data;
        }
    } catch (error) {
        console.error('레스토랑 상세 조회 실패:', error);
    }
    return null;
}

// 레스토랑 생성
async function createRestaurant(restaurantData) {
    try {
        const response = await apiCall('/v1/owners/restaurants', {
            method: 'POST',
            body: JSON.stringify(restaurantData)
        });
        if (response && response.data) {
            showNotification('식당이 성공적으로 등록되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('레스토랑 생성 실패:', error);
    }
    return null;
}

// 레스토랑 수정
async function updateRestaurant(restaurantId, restaurantData) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}`, {
            method: 'PUT',
            body: JSON.stringify(restaurantData)
        });
        if (response && response.data) {
            showNotification('식당 정보가 수정되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('레스토랑 수정 실패:', error);
    }
    return null;
}

// 레스토랑 삭제
async function deleteRestaurant(restaurantId) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}`, {
            method: 'DELETE'
        });
        if (response) {
            showNotification('식당이 삭제되었습니다!');
            return true;
        }
    } catch (error) {
        console.error('레스토랑 삭제 실패:', error);
    }
    return false;
}

// 메뉴 목록 조회
async function fetchMenus(restaurantId) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}/menus`);
        if (response && response.data) {
            currentMenus = response.data.content || [];
            return currentMenus;
        }
    } catch (error) {
        console.error('메뉴 목록 조회 실패:', error);
    }
    return [];
}

// 메뉴 생성
async function createMenu(restaurantId, menuData) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}/menus`, {
            method: 'POST',
            body: JSON.stringify(menuData)
        });
        if (response && response.data) {
            showNotification('메뉴가 성공적으로 등록되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('메뉴 생성 실패:', error);
    }
    return null;
}

// 메뉴 수정
async function updateMenu(restaurantId, menuId, menuData) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}/menus/${menuId}`, {
            method: 'PUT',
            body: JSON.stringify(menuData)
        });
        if (response && response.data) {
            showNotification('메뉴가 수정되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('메뉴 수정 실패:', error);
    }
    return null;
}

// 메뉴 삭제
async function deleteMenu(restaurantId, menuId) {
    try {
        const response = await apiCall(`/v1/owners/restaurants/${restaurantId}/menus/${menuId}`, {
            method: 'DELETE'
        });
        if (response) {
            showNotification('메뉴가 삭제되었습니다!');
            return true;
        }
    } catch (error) {
        console.error('메뉴 삭제 실패:', error);
    }
    return false;
}

// AI 메뉴 설명 생성
async function generateMenuDescription(menuInfo) {
    try {
        const response = await apiCall('/v1/owners/aiprompt/menu-description', {
            method: 'POST',
            body: JSON.stringify({ menuInfo })
        });
        if (response && response.data) {
            return response.data.responseContent;
        }
    } catch (error) {
        console.error('AI 메뉴 설명 생성 실패:', error);
    }
    return null;
}

// 결제 정보 조회
async function fetchApprovedPayments(startDate, endDate) {
    try {
        const response = await apiCall(
            `/api/v1/owner/payments/approved?startDate=${startDate}&endDate=${endDate}`
        );
        if (response && response.data) {
            return response.data;
        }
    } catch (error) {
        console.error('결제 정보 조회 실패:', error);
    }
    return [];
}

// 사용자 주소 추가
async function addUserAddress(userId, addressData) {
    try {
        const response = await apiCall(`/v1/users/${userId}/addresses`, {
            method: 'POST',
            body: JSON.stringify(addressData)
        });
        if (response && response.data) {
            showNotification('주소가 추가되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('주소 추가 실패:', error);
    }
    return null;
}

// 사용자 주소 삭제
async function deleteUserAddress(userId, index) {
    try {
        const response = await apiCall(`/v1/users/${userId}/addresses/${index}`, {
            method: 'DELETE'
        });
        if (response && response.data) {
            showNotification('주소가 삭제되었습니다!');
            return response.data;
        }
    } catch (error) {
        console.error('주소 삭제 실패:', error);
    }
    return null;
}

// ==================== UI 렌더링 함수 ====================

// 메인 레이아웃 렌더링
function renderMainLayout() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <div class="main-layout">
            <aside class="sidebar">
                <div class="user-info">
                    <h3>${currentUser ? currentUser.username : '사장님'}</h3>
                    <p>${currentUser ? currentUser.email : 'loading...'}</p>
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
async function changeMenu(menuId) {
    currentMenu = menuId;
    renderMenu();
    await renderContent(menuId);
}

// 콘텐츠 렌더링
async function renderContent(menuId) {
    const contentTitle = document.getElementById('contentTitle');
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    const menuItem = MENU_ITEMS.find(item => item.id === menuId);
    contentTitle.textContent = menuItem.label;

    contentBody.innerHTML = '<div class="loading">로딩 중...</div>';

    switch(menuId) {
        case 'my-restaurant':
            await renderMyRestaurant(contentDesc, contentBody);
            break;
        case 'payments':
            await renderPayments(contentDesc, contentBody);
            break;
        case 'profile':
            await renderProfile(contentDesc, contentBody);
            break;
        default:
            contentBody.innerHTML = `<p>준비 중입니다...</p>`;
    }
}

// ==================== 내 식당 렌더링 ====================

async function renderMyRestaurant(contentDesc, contentBody) {
    contentDesc.textContent = '식당 정보를 관리하세요';

    await fetchMyRestaurants();

    contentBody.innerHTML = `
        <div class="restaurant-section">
            <div class="section-header">
                <h3>내 식당 목록</h3>
                <button class="btn btn-primary" onclick="showRestaurantCreateModal()">
                    + 식당 등록
                </button>
            </div>
            <div id="restaurantList" class="restaurant-list">
                ${currentRestaurants.length === 0 ?
        '<p class="empty-message">등록된 식당이 없습니다.</p>' :
        currentRestaurants.map(restaurant => `
                        <div class="restaurant-card">
                            <div class="restaurant-info">
                                <h4>${restaurant.restaurantName}</h4>
                                <p class="address">${restaurant.address?.fullAddress || '주소 없음'}</p>
                                <p class="contact">${restaurant.contactNumber}</p>
                                <div class="restaurant-stats">
                                    <span class="badge ${restaurant.isActive ? 'badge-success' : 'badge-danger'}">
                                        ${restaurant.isActive ? '영업중' : '휴무'}
                                    </span>
                                    <span>리뷰: ${restaurant.reviewCount || 0}</span>
                                    <span>평점: ${restaurant.reviewRating || 0}</span>
                                </div>
                            </div>
                            <div class="restaurant-actions">
                                <button class="btn btn-sm" onclick="viewRestaurantMenus('${restaurant.restaurantId}')">
                                    메뉴 관리
                                </button>
                                <button class="btn btn-sm" onclick="showRestaurantEditModal('${restaurant.restaurantId}')">
                                    수정
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="handleDeleteRestaurant('${restaurant.restaurantId}')">
                                    삭제
                                </button>
                            </div>
                        </div>
                    `).join('')
    }
            </div>
        </div>
    `;
}

// 식당 메뉴 관리 화면
async function viewRestaurantMenus(restaurantId) {
    selectedRestaurantId = restaurantId;
    const restaurant = await fetchRestaurantDetail(restaurantId);
    await fetchMenus(restaurantId);

    const contentBody = document.getElementById('contentBody');
    contentBody.innerHTML = `
        <div class="menu-section">
            <div class="section-header">
                <div>
                    <button class="btn btn-secondary" onclick="changeMenu('my-restaurant')">
                        ← 식당 목록
                    </button>
                    <h3 style="display: inline; margin-left: 15px;">${restaurant.restaurantName} - 메뉴 관리</h3>
                </div>
                <button class="btn btn-primary" onclick="showMenuCreateModal()">
                    + 메뉴 등록
                </button>
            </div>
            <div id="menuList" class="menu-list">
                ${currentMenus.length === 0 ?
        '<p class="empty-message">등록된 메뉴가 없습니다.</p>' :
        currentMenus.map(menu => `
                        <div class="menu-card">
                            <div class="menu-info">
                                <h4>${menu.menuName}</h4>
                                <p class="description">${menu.description || ''}</p>
                                <p class="price">${menu.price?.toLocaleString()}원</p>
                                <div class="menu-stats">
                                    <span class="badge ${menu.isAvailable ? 'badge-success' : 'badge-danger'}">
                                        ${menu.isAvailable ? '판매중' : '품절'}
                                    </span>
                                    ${menu.isMain ? '<span class="badge badge-primary">대표메뉴</span>' : ''}
                                    ${menu.isPopular ? '<span class="badge badge-warning">인기메뉴</span>' : ''}
                                    ${menu.isNew ? '<span class="badge badge-info">신메뉴</span>' : ''}
                                </div>
                            </div>
                            <div class="menu-actions">
                                <button class="btn btn-sm" onclick="showMenuEditModal('${menu.menuId}')">
                                    수정
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="handleDeleteMenu('${menu.menuId}')">
                                    삭제
                                </button>
                            </div>
                        </div>
                    `).join('')
    }
            </div>
        </div>
    `;
}

// ==================== 결제 정보 렌더링 ====================

async function renderPayments(contentDesc, contentBody) {
    contentDesc.textContent = '결제 정보를 확인하세요';

    const today = new Date();
    const lastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1);

    const startDate = lastMonth.toISOString();
    const endDate = today.toISOString();

    const payments = await fetchApprovedPayments(startDate, endDate);

    contentBody.innerHTML = `
        <div class="payment-section">
            <div class="section-header">
                <h3>결제 내역</h3>
                <div class="date-filter">
                    <input type="date" id="startDate" value="${lastMonth.toISOString().split('T')[0]}">
                    <span>~</span>
                    <input type="date" id="endDate" value="${today.toISOString().split('T')[0]}">
                    <button class="btn btn-primary" onclick="filterPayments()">조회</button>
                </div>
            </div>
            <div class="payment-summary">
                <div class="summary-card">
                    <h4>총 결제 건수</h4>
                    <p class="summary-value">${payments?.length || 0}건</p>
                </div>
                <div class="summary-card">
                    <h4>총 결제 금액</h4>
                    <p class="summary-value">${(payments?.reduce((sum, p) => sum + (p.amount || 0), 0) || 0).toLocaleString()}원</p>
                </div>
            </div>
            <div class="payment-list">
                ${!payments || payments.length === 0 ?
        '<p class="empty-message">결제 내역이 없습니다.</p>' :
        `<table class="data-table">
                        <thead>
                            <tr>
                                <th>결제ID</th>
                                <th>주문ID</th>
                                <th>금액</th>
                                <th>결제수단</th>
                                <th>상태</th>
                                <th>승인일시</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${payments.map(payment => `
                                <tr>
                                    <td>${payment.paymentId}</td>
                                    <td>${payment.orderId}</td>
                                    <td>${payment.amount?.toLocaleString()}원</td>
                                    <td>${payment.paymentMethod}</td>
                                    <td><span class="badge badge-success">${payment.paymentStatus}</span></td>
                                    <td>${new Date(payment.approvedAt).toLocaleString()}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>`
    }
            </div>
        </div>
    `;
}

async function filterPayments() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        showNotification('날짜를 선택해주세요.', 'error');
        return;
    }

    const contentBody = document.getElementById('contentBody');
    const contentDesc = document.getElementById('contentDesc');
    await renderPayments(contentDesc, contentBody);
}

// ==================== 사용자 정보 렌더링 ====================

async function renderProfile(contentDesc, contentBody) {
    contentDesc.textContent = '내 정보를 관리하세요';

    if (!currentUser) {
        await fetchUserInfo();
    }

    contentBody.innerHTML = `
        <div class="profile-section">
            <div class="profile-card">
                <h3>기본 정보</h3>
                <div class="profile-info">
                    <div class="info-row">
                        <label>사용자명</label>
                        <p>${currentUser?.username || '-'}</p>
                    </div>
                    <div class="info-row">
                        <label>이메일</label>
                        <p>${currentUser?.email || '-'}</p>
                    </div>
                </div>
            </div>
            
            <div class="profile-card">
                <div class="section-header">
                    <h3>주소 목록</h3>
                    <button class="btn btn-primary btn-sm" onclick="showAddressAddModal()">
                        + 주소 추가
                    </button>
                </div>
                <div class="address-list">
                    ${!currentUser?.addresses || currentUser.addresses.length === 0 ?
        '<p class="empty-message">등록된 주소가 없습니다.</p>' :
        currentUser.addresses.map((addr, index) => `
                            <div class="address-item">
                                <div class="address-info">
                                    <p class="address-text">${addr.address}</p>
                                    <p class="address-coords">위도: ${addr.lat}, 경도: ${addr.lon}</p>
                                </div>
                                <button class="btn btn-sm btn-danger" onclick="handleDeleteAddress(${index})">
                                    삭제
                                </button>
                            </div>
                        `).join('')
    }
                </div>
            </div>
        </div>
    `;
}

// ==================== 모달 함수들 ====================

// 식당 등록 모달
async function showRestaurantCreateModal() {
    await fetchCategories();

    const modal = createModal('식당 등록', `
        <form id="restaurantForm" class="form">
            <div class="form-group">
                <label>식당명 *</label>
                <input type="text" name="restaurantName" required>
            </div>
            
            <div class="form-group">
                <label>연락처 *</label>
                <input type="text" name="contactNumber" placeholder="02-1234-5678" required>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>시/도 *</label>
                    <input type="text" name="province" required>
                </div>
                <div class="form-group">
                    <label>시/군/구 *</label>
                    <input type="text" name="city" required>
                </div>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>동/읍/면 *</label>
                    <input type="text" name="district" required>
                </div>
                <div class="form-group">
                    <label>상세주소</label>
                    <input type="text" name="detailAddress">
                </div>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>위도</label>
                    <input type="number" step="0.000001" name="latitude">
                </div>
                <div class="form-group">
                    <label>경도</label>
                    <input type="number" step="0.000001" name="longitude">
                </div>
            </div>
            
            <div class="form-group">
                <label>카테고리 *</label>
                <select name="categoryIds" multiple size="5" required>
                    ${categories.map(cat => `
                        <option value="${cat.id}">${cat.categoryName}</option>
                    `).join('')}
                </select>
                <small>Ctrl/Cmd를 누르고 클릭하여 여러 개 선택 가능</small>
            </div>
            
            <div class="form-group">
                <label>태그 (쉼표로 구분)</label>
                <input type="text" name="tags" placeholder="빠른배달, 맛집, 친절">
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">취소</button>
                <button type="submit" class="btn btn-primary">등록</button>
            </div>
        </form>
    `);

    document.getElementById('restaurantForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const selectedCategories = Array.from(
            document.querySelector('select[name="categoryIds"]').selectedOptions
        ).map(option => option.value);

        const tags = formData.get('tags')
            ? formData.get('tags').split(',').map(t => t.trim())
            : [];

        const restaurantData = {
            restaurantName: formData.get('restaurantName'),
            contactNumber: formData.get('contactNumber'),
            address: {
                province: formData.get('province'),
                city: formData.get('city'),
                district: formData.get('district'),
                detailAddress: formData.get('detailAddress') || null
            },
            coordinate: formData.get('latitude') && formData.get('longitude') ? {
                latitude: parseFloat(formData.get('latitude')),
                longitude: parseFloat(formData.get('longitude'))
            } : null,
            categoryIds: selectedCategories,
            tags: tags
        };

        const result = await createRestaurant(restaurantData);
        if (result) {
            closeModal();
            await changeMenu('my-restaurant');
        }
    };
}

// 식당 수정 모달
async function showRestaurantEditModal(restaurantId) {
    const restaurant = await fetchRestaurantDetail(restaurantId);
    if (!restaurant) return;

    await fetchCategories();

    const modal = createModal('식당 수정', `
        <form id="restaurantEditForm" class="form">
            <div class="form-group">
                <label>식당명</label>
                <input type="text" name="restaurantName" value="${restaurant.restaurantName || ''}">
            </div>
            
            <div class="form-group">
                <label>연락처</label>
                <input type="text" name="contactNumber" value="${restaurant.contactNumber || ''}" placeholder="02-1234-5678">
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>시/도</label>
                    <input type="text" name="province" value="${restaurant.address?.province || ''}">
                </div>
                <div class="form-group">
                    <label>시/군/구</label>
                    <input type="text" name="city" value="${restaurant.address?.city || ''}">
                </div>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>동/읍/면</label>
                    <input type="text" name="district" value="${restaurant.address?.district || ''}">
                </div>
                <div class="form-group">
                    <label>상세주소</label>
                    <input type="text" name="detailAddress" value="${restaurant.address?.detailAddress || ''}">
                </div>
            </div>
            
            <div class="form-group">
                <label>상태</label>
                <select name="status">
                    <option value="OPEN" ${restaurant.status === 'OPEN' ? 'selected' : ''}>영업중</option>
                    <option value="CLOSED" ${restaurant.status === 'CLOSED' ? 'selected' : ''}>휴무</option>
                    <option value="TEMPORARILY_CLOSED" ${restaurant.status === 'TEMPORARILY_CLOSED' ? 'selected' : ''}>임시휴무</option>
                    <option value="PREPARING" ${restaurant.status === 'PREPARING' ? 'selected' : ''}>준비중</option>
                </select>
            </div>
            
            <div class="form-group">
                <label>태그 (쉼표로 구분)</label>
                <input type="text" name="tags" value="${restaurant.tags?.join(', ') || ''}">
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">취소</button>
                <button type="submit" class="btn btn-primary">수정</button>
            </div>
        </form>
    `);

    document.getElementById('restaurantEditForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const tags = formData.get('tags')
            ? formData.get('tags').split(',').map(t => t.trim())
            : [];

        const restaurantData = {
            restaurantName: formData.get('restaurantName') || null,
            contactNumber: formData.get('contactNumber') || null,
            address: {
                province: formData.get('province') || null,
                city: formData.get('city') || null,
                district: formData.get('district') || null,
                detailAddress: formData.get('detailAddress') || null
            },
            status: formData.get('status') || null,
            tags: tags.length > 0 ? tags : null
        };

        const result = await updateRestaurant(restaurantId, restaurantData);
        if (result) {
            closeModal();
            await changeMenu('my-restaurant');
        }
    };
}

// 메뉴 등록 모달
async function showMenuCreateModal() {
    const modal = createModal('메뉴 등록', `
        <form id="menuForm" class="form">
            <div class="form-group">
                <label>메뉴명 *</label>
                <input type="text" name="menuName" required>
            </div>
            
            <div class="form-group">
                <label>가격 *</label>
                <input type="number" name="price" min="0" required>
            </div>
            
            <div class="form-group">
                <label>설명</label>
                <textarea name="description" rows="3" id="menuDescription"></textarea>
                <button type="button" class="btn btn-secondary btn-sm" onclick="generateAIDescription()" style="margin-top: 5px;">
                    ✨ AI로 설명 생성
                </button>
            </div>
            
            <div class="form-group">
                <label>재료</label>
                <textarea name="ingredients" rows="2"></textarea>
            </div>
            
            <div class="form-group">
                <label>칼로리</label>
                <input type="number" name="calorie" min="0">
            </div>
            
            <div class="form-group">
                <label class="checkbox-label">
                    <input type="checkbox" name="isMain">
                    대표 메뉴
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" name="isPopular">
                    인기 메뉴
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" name="isNew">
                    신메뉴
                </label>
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">취소</button>
                <button type="submit" class="btn btn-primary">등록</button>
            </div>
        </form>
    `);

    document.getElementById('menuForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const menuData = {
            menuName: formData.get('menuName'),
            description: formData.get('description') || null,
            ingredients: formData.get('ingredients') || null,
            price: parseFloat(formData.get('price')),
            calorie: formData.get('calorie') ? parseInt(formData.get('calorie')) : null,
            isMain: formData.get('isMain') === 'on',
            isPopular: formData.get('isPopular') === 'on',
            isNew: formData.get('isNew') === 'on'
        };

        const result = await createMenu(selectedRestaurantId, menuData);
        if (result) {
            closeModal();
            await viewRestaurantMenus(selectedRestaurantId);
        }
    };
}

// AI 설명 생성
async function generateAIDescription() {
    const menuName = document.querySelector('input[name="menuName"]').value;
    const ingredients = document.querySelector('textarea[name="ingredients"]').value;

    if (!menuName) {
        showNotification('메뉴명을 먼저 입력해주세요.', 'error');
        return;
    }

    const menuInfo = `메뉴명: ${menuName}${ingredients ? '\n재료: ' + ingredients : ''}`;

    showNotification('AI가 설명을 생성하고 있습니다...', 'info');

    const description = await generateMenuDescription(menuInfo);

    if (description) {
        document.getElementById('menuDescription').value = description;
        showNotification('AI 설명이 생성되었습니다!');
    }
}

// 메뉴 수정 모달
async function showMenuEditModal(menuId) {
    const menu = currentMenus.find(m => m.menuId === menuId);
    if (!menu) return;

    const modal = createModal('메뉴 수정', `
        <form id="menuEditForm" class="form">
            <div class="form-group">
                <label>메뉴명</label>
                <input type="text" name="menuName" value="${menu.menuName || ''}">
            </div>
            
            <div class="form-group">
                <label>가격</label>
                <input type="number" name="price" value="${menu.price || ''}" min="0">
            </div>
            
            <div class="form-group">
                <label>설명</label>
                <textarea name="description" rows="3">${menu.description || ''}</textarea>
            </div>
            
            <div class="form-group">
                <label>재료</label>
                <textarea name="ingredients" rows="2">${menu.ingredients || ''}</textarea>
            </div>
            
            <div class="form-group">
                <label>칼로리</label>
                <input type="number" name="calorie" value="${menu.calorie || ''}" min="0">
            </div>
            
            <div class="form-group">
                <label class="checkbox-label">
                    <input type="checkbox" name="isAvailable" ${menu.isAvailable ? 'checked' : ''}>
                    판매 가능
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" name="isMain" ${menu.isMain ? 'checked' : ''}>
                    대표 메뉴
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" name="isPopular" ${menu.isPopular ? 'checked' : ''}>
                    인기 메뉴
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" name="isNew" ${menu.isNew ? 'checked' : ''}>
                    신메뉴
                </label>
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">취소</button>
                <button type="submit" class="btn btn-primary">수정</button>
            </div>
        </form>
    `);

    document.getElementById('menuEditForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const menuData = {
            menuName: formData.get('menuName') || null,
            description: formData.get('description') || null,
            ingredients: formData.get('ingredients') || null,
            price: formData.get('price') ? parseFloat(formData.get('price')) : null,
            calorie: formData.get('calorie') ? parseInt(formData.get('calorie')) : null,
            isAvailable: formData.get('isAvailable') === 'on',
            isMain: formData.get('isMain') === 'on',
            isPopular: formData.get('isPopular') === 'on',
            isNew: formData.get('isNew') === 'on'
        };

        const result = await updateMenu(selectedRestaurantId, menuId, menuData);
        if (result) {
            closeModal();
            await viewRestaurantMenus(selectedRestaurantId);
        }
    };
}

// 주소 추가 모달
function showAddressAddModal() {
    const modal = createModal('주소 추가', `
        <form id="addressForm" class="form">
            <div class="form-group">
                <label>주소 *</label>
                <input type="text" name="address" required>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>위도 *</label>
                    <input type="number" step="0.000001" name="lat" required>
                </div>
                <div class="form-group">
                    <label>경도 *</label>
                    <input type="number" step="0.000001" name="lon" required>
                </div>
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">취소</button>
                <button type="submit" class="btn btn-primary">추가</button>
            </div>
        </form>
    `);

    document.getElementById('addressForm').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const addressData = {
            address: formData.get('address'),
            lat: parseFloat(formData.get('lat')),
            lon: parseFloat(formData.get('lon'))
        };

        // userId는 실제 구현에서 가져와야 함
        const userId = 'current-user-id'; // TODO: 실제 userId로 교체
        const result = await addUserAddress(userId, addressData);

        if (result) {
            currentUser.addresses = result;
            closeModal();
            await changeMenu('profile');
        }
    };
}

// ==================== 삭제 핸들러 ====================

async function handleDeleteRestaurant(restaurantId) {
    if (!confirm('정말 이 식당을 삭제하시겠습니까?')) return;

    const result = await deleteRestaurant(restaurantId);
    if (result) {
        await changeMenu('my-restaurant');
    }
}

async function handleDeleteMenu(menuId) {
    if (!confirm('정말 이 메뉴를 삭제하시겠습니까?')) return;

    const result = await deleteMenu(selectedRestaurantId, menuId);
    if (result) {
        await viewRestaurantMenus(selectedRestaurantId);
    }
}

async function handleDeleteAddress(index) {
    if (!confirm('정말 이 주소를 삭제하시겠습니까?')) return;

    const userId = 'current-user-id'; // TODO: 실제 userId로 교체
    const result = await deleteUserAddress(userId, index);

    if (result) {
        currentUser.addresses = result;
        await changeMenu('profile');
    }
}

// ==================== 모달 유틸리티 ====================

function createModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    return modal;
}

function closeModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
        modal.remove();
    }
}

// ==================== 초기화 ====================

async function initialize() {
    const token = getToken();

    if (!token) {
        window.location.href = "/view/owner/login";
        return;
    }

    // 사용자 정보 로드
    await fetchUserInfo();

    // 메인 레이아웃 렌더링
    renderMainLayout();
}

// 페이지 로드 시 초기화
initialize();