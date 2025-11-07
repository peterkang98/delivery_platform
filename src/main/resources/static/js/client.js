// ============ 전역 변수 및 설정 ============
const urlParams = new URLSearchParams(window.location.search);
const API_BASE_URL = '/v1';

let currentMenu = 'restaurant';
let selectedRestaurant = null;
let cart = []; // 장바구니

// ============ 유틸리티 함수 ============
function getRoleTypeFromPath() {
    const path = window.location.pathname;
    if (path.includes('/client')) return 'CLIENT';
    if (path.includes('/owner')) return 'OWNER';
    if (path.includes('/admin')) return 'ADMIN';
    return null;
}

function getTokenKey() {
    return `authToken_${getRoleTypeFromPath()}`;
}

function getToken() {
    return localStorage.getItem(getTokenKey());
}

function removeToken() {
    localStorage.removeItem(getTokenKey());
}

function logout() {
    removeToken();
    window.location.href = "/view/client/login";
}

// API 요청 헬퍼
async function fetchAPI(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'API 요청 실패' }));
        throw new Error(error.message || 'API 요청 실패');
    }

    return response.json();
}

// ============ 메뉴 정의 ============
const MENU_ITEMS = [
    { id: 'restaurant', label: '식당' },
    { id: 'wishlist', label: '찜' },
    { id: 'cart', label: '장바구니' },
    { id: 'orders', label: '주문정보' },
    { id: 'profile', label: '사용자 정보' },
    { id: 'qna', label: 'Q&A' },
    { id: 'payment', label: '결제 테스트' }
];

// ============ 메인 레이아웃 ============
function renderMainLayout() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <div class="main-layout">
            <aside class="sidebar">
                <div class="user-info">
                    <h3>고객님</h3>
                    <p>user@example.com</p>
                    <button class="logout-btn" onclick="logout()">로그아웃</button>
                </div>
                <nav class="nav-menu">
                    <ul id="menuList"></ul>
                </nav>
            </aside>
            <main class="main-content">
                <div class="content-header">
                    <h2 id="contentTitle">식당</h2>
                    <p id="contentDesc">식당 목록을 확인하세요</p>
                </div>
                <div class="content-body" id="contentBody"></div>
            </main>
        </div>
        
        <!-- 모달들 -->
        <div class="modal" id="restaurantDetailModal">
            <div class="modal-content" style="max-width: 800px;">
                <div class="modal-header">
                    <h3>식당 상세 정보</h3>
                    <button class="modal-close" onclick="closeModal('restaurantDetailModal')">&times;</button>
                </div>
                <div class="modal-body" id="restaurantDetailBody"></div>
            </div>
        </div>
        
        <div class="modal" id="paymentModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>결제 정보</h3>
                    <button class="modal-close" onclick="closePaymentModal()">&times;</button>
                </div>
                <div class="modal-body" id="paymentModalBody"></div>
            </div>
        </div>
    `;

    renderMenu();
    renderContent(currentMenu);
    checkPaymentCallback();
}

function renderMenu() {
    const menuList = document.getElementById('menuList');
    menuList.innerHTML = MENU_ITEMS.map(item => `
        <li class="${currentMenu === item.id ? 'active' : ''}" onclick="changeMenu('${item.id}')">
            ${item.label}
            ${item.id === 'cart' && cart.length > 0 ? ` (${cart.length})` : ''}
        </li>
    `).join('');
}

function changeMenu(menuId) {
    currentMenu = menuId;
    renderMenu();
    renderContent(menuId);
}

// ============ 콘텐츠 렌더링 ============
async function renderContent(menuId) {
    const contentTitle = document.getElementById('contentTitle');
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    const menuItem = MENU_ITEMS.find(item => item.id === menuId);
    contentTitle.textContent = menuItem.label;

    try {
        switch(menuId) {
            case 'restaurant':
                await renderRestaurantList();
                break;
            case 'wishlist':
                await renderWishlist();
                break;
            case 'cart':
                renderCart();
                break;
            case 'orders':
                await renderOrders();
                break;
            case 'profile':
                contentDesc.textContent = '내 정보를 관리하세요';
                contentBody.innerHTML = '<p>사용자 정보 관리 기능 준비 중입니다...</p>';
                break;
            case 'qna':
                contentDesc.textContent = '궁금한 점을 문의하세요';
                contentBody.innerHTML = '<p>Q&A 기능 준비 중입니다...</p>';
                break;
            case 'payment':
                contentDesc.textContent = 'Toss Payments 결제를 테스트하세요';
                renderPaymentTest();
                break;
            default:
                contentBody.innerHTML = '<p>준비 중입니다...</p>';
        }
    } catch (error) {
        console.error('콘텐츠 렌더링 오류:', error);
        contentBody.innerHTML = `<p style="color: red;">오류가 발생했습니다: ${error.message}</p>`;
    }
}

// ============ 식당 목록 ============
async function renderRestaurantList() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = '식당 목록을 확인하세요';
    contentBody.innerHTML = '<p>로딩 중...</p>';

    try {
        const response = await fetchAPI(`${API_BASE_URL}/common/restaurants?size=20`);
        const restaurants = response.data.content;

        if (restaurants.length === 0) {
            contentBody.innerHTML = '<p>등록된 식당이 없습니다.</p>';
            return;
        }

        contentBody.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px;">
                ${restaurants.map(restaurant => `
                    <div class="restaurant-card" style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; cursor: pointer;" 
                         onclick="showRestaurantDetail('${restaurant.restaurantId}')">
                        <h3 style="margin: 0 0 10px 0;">${restaurant.restaurantName}</h3>
                        <p style="color: #666; font-size: 14px; margin: 5px 0;">
                            ${restaurant.fullAddress || `${restaurant.province} ${restaurant.city} ${restaurant.district}`}
                        </p>
                        <div style="display: flex; gap: 10px; margin-top: 10px; flex-wrap: wrap;">
                            ${restaurant.categoryNames ? restaurant.categoryNames.map(cat =>
            `<span style="background: #f0f0f0; padding: 4px 8px; border-radius: 4px; font-size: 12px;">${cat}</span>`
        ).join('') : ''}
                        </div>
                        <div style="margin-top: 15px; display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                ${restaurant.reviewRating ? `⭐ ${restaurant.reviewRating} (${restaurant.reviewCount})` : '리뷰 없음'}
                            </div>
                            <div style="color: ${restaurant.isOpenNow ? '#10b981' : '#ef4444'};">
                                ${restaurant.isOpenNow ? '영업중' : '영업종료'}
                            </div>
                        </div>
                        <div style="margin-top: 10px; font-size: 13px; color: #888;">
                            ❤️ ${restaurant.wishlistCount || 0}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (error) {
        contentBody.innerHTML = `<p style="color: red;">식당 목록을 불러오는데 실패했습니다: ${error.message}</p>`;
    }
}

// ============ 식당 상세 정보 ============
async function showRestaurantDetail(restaurantId) {
    try {
        const response = await fetchAPI(`${API_BASE_URL}/common/restaurants/${restaurantId}`);
        const restaurant = response.data;

        // 찜 개수 조회
        const favoriteCountResponse = await fetchAPI(`${API_BASE_URL}/common/favorites/restaurant/${restaurantId}/count`);
        const favoriteCount = favoriteCountResponse.data;

        // 찜 여부 확인 (인증된 경우만)
        let isFavorite = false;
        if (getToken()) {
            try {
                const favoriteCheckResponse = await fetchAPI(`${API_BASE_URL}/customers/favorites/check/restaurant/${restaurantId}`);
                isFavorite = favoriteCheckResponse.data.isFavorite;
            } catch (e) {
                console.log('찜 여부 확인 실패 (로그인 필요)');
            }
        }

        selectedRestaurant = restaurant;

        // 메뉴 목록 조회
        const menuResponse = await fetchAPI(`${API_BASE_URL}/common/restaurants/${restaurantId}/menus?size=50`);
        const menus = menuResponse.data.content;

        document.getElementById('restaurantDetailBody').innerHTML = `
            <div style="margin-bottom: 20px;">
                <h2 style="margin-bottom: 10px;">${restaurant.restaurantName}</h2>
                <p style="color: #666;">${restaurant.address.fullAddress}</p>
                <div style="margin-top: 10px;">
                    <button class="btn btn-primary" onclick="toggleFavorite('${restaurantId}', ${isFavorite})" id="favoriteBtn">
                        ${isFavorite ? '❤️ 찜 취소' : '🤍 찜하기'} (${favoriteCount})
                    </button>
                </div>
            </div>
            
            <div style="margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px;">
                <h4>영업 정보</h4>
                <p>상태: <span style="color: ${restaurant.isOpenNow ? '#10b981' : '#ef4444'};">
                    ${restaurant.isOpenNow ? '영업중' : '영업종료'}
                </span></p>
                <p>연락처: ${restaurant.contactNumber}</p>
                <p>⭐ ${restaurant.reviewRating || '0'} (리뷰 ${restaurant.reviewCount || 0}개)</p>
            </div>
            
            <h3 style="margin: 20px 0 15px 0;">메뉴</h3>
            <div style="display: grid; gap: 15px;">
                ${menus.length > 0 ? menus.map(menu => `
                    <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; display: flex; justify-content: space-between; align-items: center;">
                        <div style="flex: 1;">
                            <h4 style="margin: 0 0 5px 0;">${menu.menuName}</h4>
                            <p style="color: #666; font-size: 14px; margin: 5px 0;">${menu.description || ''}</p>
                            <p style="font-weight: bold; color: #10b981; margin-top: 10px;">${menu.price.toLocaleString()}원</p>
                            ${menu.isAvailable === false ? '<span style="color: red; font-size: 12px;">품절</span>' : ''}
                        </div>
                        <button class="btn btn-primary" onclick="addToCart('${menu.menuId}', '${menu.menuName}', ${menu.price}, ${menu.isAvailable})"
                                ${!menu.isAvailable ? 'disabled' : ''}>
                            담기
                        </button>
                    </div>
                `).join('') : '<p>등록된 메뉴가 없습니다.</p>'}
            </div>
        `;

        document.getElementById('restaurantDetailModal').classList.add('show');
    } catch (error) {
        alert('식당 정보를 불러오는데 실패했습니다: ' + error.message);
    }
}

// ============ 찜하기 토글 ============
async function toggleFavorite(restaurantId, isFavorite) {
    if (!getToken()) {
        alert('로그인이 필요합니다.');
        return;
    }

    try {
        if (isFavorite) {
            // 찜 취소 - 먼저 favoriteId 찾기
            const favoritesResponse = await fetchAPI(`${API_BASE_URL}/customers/favorites`);
            const favorite = favoritesResponse.data.find(f => f.restaurantId === restaurantId && f.type === 'RESTAURANT');

            if (favorite) {
                await fetchAPI(`${API_BASE_URL}/customers/favorites/${favorite.id}`, { method: 'DELETE' });
                alert('찜이 취소되었습니다.');
            }
        } else {
            // 찜 추가
            await fetchAPI(`${API_BASE_URL}/customers/favorites`, {
                method: 'POST',
                body: JSON.stringify({
                    type: 'RESTAURANT',
                    restaurantId: restaurantId
                })
            });
            alert('찜했습니다!');
        }

        // 모달 다시 로드
        closeModal('restaurantDetailModal');
        showRestaurantDetail(restaurantId);
    } catch (error) {
        alert('찜하기 처리 중 오류가 발생했습니다: ' + error.message);
    }
}

// ============ 장바구니 ============
function addToCart(menuId, menuName, price, isAvailable) {
    if (!isAvailable) {
        alert('품절된 메뉴입니다.');
        return;
    }

    const existingItem = cart.find(item => item.menuId === menuId);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            menuId,
            menuName,
            basePrice: price,
            quantity: 1,
            restaurant: {
                restaurantId: selectedRestaurant.restaurantId,
                restaurantName: selectedRestaurant.restaurantName,
                phone: selectedRestaurant.contactNumber,
                address: {
                    province: selectedRestaurant.address.province,
                    city: selectedRestaurant.address.city,
                    district: selectedRestaurant.address.district,
                    detailAddress: selectedRestaurant.address.detailAddress,
                    coordinate: {
                        latitude: selectedRestaurant.coordinate.latitude,
                        longitude: selectedRestaurant.coordinate.longitude
                    }
                }
            }
        });
    }

    renderMenu();
    alert(`${menuName}이(가) 장바구니에 담겼습니다.`);
}

function renderCart() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = '장바구니를 확인하세요';

    if (cart.length === 0) {
        contentBody.innerHTML = '<p>장바구니가 비어있습니다.</p>';
        return;
    }

    const totalPrice = cart.reduce((sum, item) => sum + (item.basePrice * item.quantity), 0);

    contentBody.innerHTML = `
        <div style="margin-bottom: 20px;">
            ${cart.map((item, index) => `
                <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; margin-bottom: 10px;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h4 style="margin: 0 0 5px 0;">${item.menuName}</h4>
                            <p style="color: #666; margin: 5px 0;">${item.basePrice.toLocaleString()}원</p>
                        </div>
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <button onclick="updateCartQuantity(${index}, -1)" class="btn">-</button>
                            <span>${item.quantity}</span>
                            <button onclick="updateCartQuantity(${index}, 1)" class="btn">+</button>
                            <button onclick="removeFromCart(${index})" class="btn btn-danger">삭제</button>
                        </div>
                    </div>
                    <p style="font-weight: bold; margin-top: 10px;">
                        소계: ${(item.basePrice * item.quantity).toLocaleString()}원
                    </p>
                </div>
            `).join('')}
        </div>
        
        <div style="border-top: 2px solid #333; padding-top: 20px; margin-top: 20px;">
            <h3>총 금액: ${totalPrice.toLocaleString()}원</h3>
            <button class="btn btn-primary" onclick="proceedToOrder()" style="width: 100%; margin-top: 20px; padding: 15px; font-size: 16px;">
                주문하기
            </button>
        </div>
    `;
}

function updateCartQuantity(index, delta) {
    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }
    renderCart();
    renderMenu();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    renderCart();
    renderMenu();
}

// ============ 주문 처리 ============
async function proceedToOrder() {
    if (!getToken()) {
        alert('로그인이 필요합니다.');
        return;
    }

    if (cart.length === 0) {
        alert('장바구니가 비어있습니다.');
        return;
    }

    // 토스 결제 진행
    alert('결제를 진행합니다.');
    await initializePaymentForOrder();
}

async function initializePaymentForOrder() {
    const clientKey = "test_ck_yZqmkKeP8g4baNqxOKLp3bQRxB9l";
    const tossPayments = await TossPayments(clientKey);

    let customerKey = localStorage.getItem("customerKey");
    if (!customerKey) {
        customerKey = crypto.randomUUID();
        localStorage.setItem("customerKey", customerKey);
    }

    const payment = tossPayments.payment({ customerKey });

    const totalAmount = cart.reduce((sum, item) => sum + (item.basePrice * item.quantity), 0);
    const orderId = generateOrderId();

    // 주문 정보를 sessionStorage에 임시 저장
    sessionStorage.setItem('pendingOrder', JSON.stringify({
        orderId,
        items: cart,
        totalAmount
    }));

    try {
        await payment.requestPayment({
            method: "CARD",
            amount: {
                currency: "KRW",
                value: totalAmount
            },
            orderId: orderId,
            orderName: `${cart[0].menuName} 외 ${cart.length - 1}건`,
            customerName: "사용자",
            successUrl: `${window.location.origin}${window.location.pathname}?orderSuccess=true`,
            failUrl: `${window.location.origin}${window.location.pathname}?orderFail=true`,
        });
    } catch (error) {
        console.error("결제 오류:", error);
        alert("결제 요청 중 오류가 발생했습니다.");
    }
}

// 결제 완료 후 주문 생성
async function createOrderAfterPayment(paymentKey, orderId, amount) {
    const pendingOrder = JSON.parse(sessionStorage.getItem('pendingOrder'));

    if (!pendingOrder) {
        alert('주문 정보를 찾을 수 없습니다.');
        return;
    }

    try {
        const orderData = {
            orderer: {
                userId: "current-user-id", // 실제로는 토큰에서 추출
                name: "사용자",
                phone: "010-1234-5678",
                deliveryRequest: "문 앞에 놓아주세요",
                address: {
                    province: "서울특별시",
                    city: "강남구",
                    district: "역삼동",
                    detailAddress: "123-45",
                    coordinate: {
                        latitude: 37.5665,
                        longitude: 126.9780
                    }
                }
            },
            items: pendingOrder.items,
            paymentKey: paymentKey
        };

        const response = await fetchAPI(`${API_BASE_URL}/customers/orders`, {
            method: 'POST',
            body: JSON.stringify(orderData)
        });

        // 주문 성공
        cart = [];
        sessionStorage.removeItem('pendingOrder');
        renderMenu();

        alert('주문이 완료되었습니다!');
        changeMenu('orders');

    } catch (error) {
        alert('주문 생성 실패: ' + error.message);
    }
}

// ============ 주문 목록 ============
async function renderOrders() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = '주문 내역을 확인하세요';
    contentBody.innerHTML = '<p>로딩 중...</p>';

    if (!getToken()) {
        contentBody.innerHTML = '<p>로그인이 필요합니다.</p>';
        return;
    }

    try {
        const response = await fetchAPI(`${API_BASE_URL}/customers/orders?size=20`);
        const orders = response.content;

        if (orders.length === 0) {
            contentBody.innerHTML = '<p>주문 내역이 없습니다.</p>';
            return;
        }

        contentBody.innerHTML = `
            <div style="display: grid; gap: 15px;">
                ${orders.map(order => `
                    <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                        <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                            <div>
                                <h4 style="margin: 0 0 5px 0;">주문번호: ${order.orderId}</h4>
                                <p style="color: #666; font-size: 14px; margin: 0;">
                                    ${new Date(order.createdAt).toLocaleString()}
                                </p>
                            </div>
                            <span style="padding: 5px 10px; background: #f0f0f0; border-radius: 4px; font-size: 14px;">
                                ${getOrderStatusText(order.status)}
                            </span>
                        </div>
                        
                        <div style="margin-bottom: 15px;">
                            ${order.items.map(item => `
                                <div style="margin-bottom: 10px;">
                                    <p style="margin: 0; font-weight: 500;">${item.menuName} x ${item.quantity}</p>
                                    <p style="margin: 5px 0 0 0; color: #666; font-size: 14px;">
                                        ${item.basePrice.toLocaleString()}원
                                    </p>
                                </div>
                            `).join('')}
                        </div>
                        
                        <div style="border-top: 1px solid #e0e0e0; padding-top: 15px; display: flex; justify-content: space-between; align-items: center;">
                            <p style="font-weight: bold; font-size: 16px; margin: 0;">
                                총 금액: ${order.totalPrice.toLocaleString()}원
                            </p>
                            <button class="btn btn-primary" onclick="showOrderDetail('${order.orderId}')">
                                상세보기
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (error) {
        contentBody.innerHTML = `<p style="color: red;">주문 내역을 불러오는데 실패했습니다: ${error.message}</p>`;
    }
}

function getOrderStatusText(status) {
    const statusMap = {
        'PENDING': '대기중',
        'PAYMENT_COMPLETED': '결제완료',
        'CONFIRMED': '접수완료',
        'PREPARING': '준비중',
        'DELIVERING': '배달중',
        'COMPLETED': '완료',
        'CANCELLED': '취소됨'
    };
    return statusMap[status] || status;
}

async function showOrderDetail(orderId) {
    try {
        const orderResponse = await fetchAPI(`${API_BASE_URL}/customers/orders/${orderId}`);
        const order = orderResponse;

        // 결제 정보 조회
        let paymentInfo = null;
        try {
            const paymentResponse = await fetchAPI(`${API_BASE_URL}/customers/payments/order/${orderId}`);
            paymentInfo = paymentResponse.data;
        } catch (e) {
            console.log('결제 정보 조회 실패');
        }

        alert(`주문 상세 정보\n\n주문번호: ${order.orderId}\n상태: ${getOrderStatusText(order.status)}\n총 금액: ${order.totalPrice.toLocaleString()}원\n${paymentInfo ? `\n결제 상태: ${paymentInfo.paymentStatus}` : ''}`);
    } catch (error) {
        alert('주문 정보를 불러오는데 실패했습니다: ' + error.message);
    }
}

// ============ 찜 목록 ============
async function renderWishlist() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = '찜한 식당을 확인하세요';
    contentBody.innerHTML = '<p>로딩 중...</p>';

    if (!getToken()) {
        contentBody.innerHTML = '<p>로그인이 필요합니다.</p>';
        return;
    }

    try {
        const response = await fetchAPI(`${API_BASE_URL}/customers/favorites`);
        const favorites = response.data;

        const restaurantFavorites = favorites.filter(f => f.type === 'RESTAURANT');

        if (restaurantFavorites.length === 0) {
            contentBody.innerHTML = '<p>찜한 식당이 없습니다.</p>';
            return;
        }

        // 각 식당 정보 조회
        const restaurantPromises = restaurantFavorites.map(fav =>
            fetchAPI(`${API_BASE_URL}/common/restaurants/${fav.restaurantId}`)
        );

        const restaurantResponses = await Promise.all(restaurantPromises);
        const restaurants = restaurantResponses.map(r => r.data);

        contentBody.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px;">
                ${restaurants.map((restaurant, index) => `
                    <div class="restaurant-card" style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                        <h3 style="margin: 0 0 10px 0;">${restaurant.restaurantName}</h3>
                        <p style="color: #666; font-size: 14px; margin: 5px 0;">
                            ${restaurant.address.fullAddress}
                        </p>
                        <div style="margin-top: 15px; display: flex; gap: 10px;">
                            <button class="btn btn-primary" onclick="showRestaurantDetail('${restaurant.restaurantId}')">
                                상세보기
                            </button>
                            <button class="btn btn-danger" onclick="removeFavorite('${restaurantFavorites[index].id}')">
                                찜 취소
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (error) {
        contentBody.innerHTML = `<p style="color: red;">찜 목록을 불러오는데 실패했습니다: ${error.message}</p>`;
    }
}

async function removeFavorite(favoriteId) {
    try {
        await fetchAPI(`${API_BASE_URL}/customers/favorites/${favoriteId}`, { method: 'DELETE' });
        alert('찜이 취소되었습니다.');
        renderWishlist();
    } catch (error) {
        alert('찜 취소 실패: ' + error.message);
    }
}

// ============ 결제 테스트 ============
function renderPaymentTest() {
    const contentBody = document.getElementById('contentBody');
    contentBody.innerHTML = `
        <h3>결제 테스트</h3>
        <div style="margin: 20px 0;">
            <p>Toss Payments 결제 시스템을 테스트합니다.</p>
            <button class="btn btn-primary" id="payButton" style="margin-top: 20px;">
                100원 결제하기
            </button>
        </div>
    `;

    setupPayment();
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

    document.getElementById("payButton").addEventListener("click", async () => {
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
                successUrl: `${window.location.origin}${window.location.pathname}?payment=success`,
                failUrl: `${window.location.origin}${window.location.pathname}?payment=fail`,
            });
        } catch (error) {
            console.error("결제 오류:", error);
            alert("결제 요청 중 오류가 발생했습니다.");
        }
    });
}

// ============ 결제 콜백 ============
function checkPaymentCallback() {
    const paymentKey = urlParams.get('paymentKey');
    const orderId = urlParams.get('orderId');
    const amount = urlParams.get('amount');
    const orderSuccess = urlParams.get('orderSuccess');
    const orderFail = urlParams.get('orderFail');

    if (orderSuccess && paymentKey && orderId) {
        // 주문 결제 성공
        createOrderAfterPayment(paymentKey, orderId, amount);
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (orderFail) {
        // 주문 결제 실패
        alert('결제가 취소되었습니다.');
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (paymentKey && orderId) {
        // 일반 결제 테스트 성공
        showPaymentResult({
            success: true,
            paymentKey: paymentKey,
            orderId: orderId,
            amount: amount
        });
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

function showPaymentResult(result) {
    const modalBody = document.getElementById('paymentModalBody');

    if (result.success) {
        modalBody.innerHTML = `
            <div style="border:1px solid #10b981; padding:20px; border-radius:8px; background:#f0fdf4;">
                <h3 style="color:#10b981; margin-bottom:15px;">✅ 결제 완료</h3>
                <p><strong>결제 금액:</strong> ${result.amount}원</p>
                <p><strong>주문번호:</strong> ${result.orderId}</p>
                <p><strong>paymentKey:</strong> ${result.paymentKey}</p>
            </div>
        `;
    }

    document.getElementById('paymentModal').classList.add('show');
}

function closePaymentModal() {
    document.getElementById('paymentModal').classList.remove('show');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
}

function generateOrderId() {
    return "order_" + Math.random().toString(36).slice(2, 11);
}

// ============ 페이지 로드 ============
const token = getToken();

if (!token) {
    window.location.href = "/view/client/login";
} else {
    renderMainLayout();
}