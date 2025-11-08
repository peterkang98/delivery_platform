// ============ 전역 변수 및 설정 ============
const urlParams = new URLSearchParams(window.location.search);
const API_BASE_URL = '/v1';

let currentMenu = 'restaurant';
let selectedRestaurant = null;
let cart = []; // 장바구니
let currentUser = null; // 현재 사용자 정보
let selectedAddress = null; // 선택된 배송 주소

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

// 사용자 정보 로드
async function loadUserInfo() {
    try {
        const response = await fetchAPI(`${API_BASE_URL}/customers/my-info`);
        currentUser = response.data;
        return currentUser;
    } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        return null;
    }
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
        
        <div class="modal" id="addressModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>배송지 주소 등록</h3>
                    <button class="modal-close" onclick="closeModal('addressModal')">&times;</button>
                </div>
                <div class="modal-body" id="addressModalBody">
                    <div style="margin-bottom: 20px;">
                        <label style="display: block; margin-bottom: 10px; font-weight: 500;">주소 검색</label>
                        <button class="btn btn-primary" onclick="openDaumPostcode()">우편번호 찾기</button>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px; color: #666;">우편번호</label>
                        <input type="text" id="zonecode" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; background: #f5f5f5;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px; color: #666;">도로명 주소</label>
                        <input type="text" id="roadAddress" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; background: #f5f5f5;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px; color: #666;">지번 주소</label>
                        <input type="text" id="jibunAddress" readonly style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; background: #f5f5f5;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px; color: #666;">상세 주소</label>
                        <input type="text" id="detailAddress" placeholder="상세 주소를 입력하세요" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                    </div>
                    <div style="margin-bottom: 15px; display: none;">
                        <label style="display: block; margin-bottom: 5px; color: #666;">좌표 (자동 입력됨)</label>
                        <div style="display: flex; gap: 10px;">
                            <input type="text" id="latitude" readonly placeholder="위도" style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 4px; background: #f5f5f5;">
                            <input type="text" id="longitude" readonly placeholder="경도" style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 4px; background: #f5f5f5;">
                        </div>
                    </div>
                    <div style="display: flex; gap: 10px; margin-top: 20px;">
                        <button class="btn btn-primary" onclick="saveAddress()" style="flex: 1;">저장</button>
                        <button class="btn" onclick="closeModal('addressModal')" style="flex: 1;">취소</button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="modal" id="orderConfirmModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>주문 정보 확인</h3>
                    <button class="modal-close" onclick="closeModal('orderConfirmModal')">&times;</button>
                </div>
                <div class="modal-body" id="orderConfirmBody"></div>
            </div>
        </div>
        
        <div class="modal" id="cancelOrderModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>주문 취소</h3>
                    <button class="modal-close" onclick="closeModal('cancelOrderModal')">&times;</button>
                </div>
                <div class="modal-body" id="cancelOrderBody">
                    <p style="margin-bottom: 15px; color: #666;">주문을 취소하시려는 이유를 알려주세요.</p>
                    <textarea id="cancelReason" 
                              style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; min-height: 100px; resize: vertical;"
                              placeholder="취소 사유를 입력해주세요 (필수)"></textarea>
                    <div style="display: flex; gap: 10px; margin-top: 20px;">
                        <button class="btn btn-danger" onclick="submitCancelOrder()" style="flex: 1;">
                            취소 확정
                        </button>
                        <button class="btn" onclick="closeModal('cancelOrderModal')" style="flex: 1;">
                            돌아가기
                        </button>
                    </div>
                </div>
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

// ============ Daum 우편번호 API ============
function openDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            console.log('Daum Postcode 데이터:', data);

            // 우편번호와 주소 정보를 입력
            document.getElementById('zonecode').value = data.zonecode;

            // 도로명 주소 우선, 없으면 지번 주소
            let fullAddress = data.roadAddress || data.autoJibunAddress;

            // 사용자가 선택한 주소 타입에 따라 처리
            if (data.userSelectedType === 'R') {
                // 도로명 주소 선택
                document.getElementById('roadAddress').value = data.roadAddress;
                document.getElementById('jibunAddress').value = data.autoJibunAddress || '';
            } else {
                // 지번 주소 선택
                document.getElementById('roadAddress').value = data.roadAddress || '';
                document.getElementById('jibunAddress').value = data.autoJibunAddress;
            }

            // 상세주소 입력란으로 포커스 이동
            document.getElementById('detailAddress').focus();

            // Kakao 지도 API로 좌표 변환 (선택한 주소 사용)
            getCoordinates(fullAddress).catch(error => {
                console.error('좌표 변환 실패:', error);
                // 좌표 변환 실패해도 주소는 저장 가능하도록 함
            });
        }
    }).open();
}

// ============ VWorld Geocoder API - 주소로 좌표 검색 ============
const VWORLD_API_KEY = 'C28A8E80-ACB8-31C0-920F-F8CB6E40CE74';

async function getCoordinates(address) {
    return new Promise((resolve, reject) => {
        // JSONP 콜백 함수 이름 생성
        const callbackName = 'vworldCallback_' + Date.now();

        // 전역 콜백 함수 등록
        window[callbackName] = function(result) {
            // 콜백 함수 정리
            delete window[callbackName];
            document.body.removeChild(script);

            console.log('VWorld API 응답:', result);

            if (result.response && result.response.status === 'OK' && result.response.result) {
                const point = result.response.result.point;
                const latitude = parseFloat(point.y);
                const longitude = parseFloat(point.x);

                document.getElementById('latitude').value = latitude;
                document.getElementById('longitude').value = longitude;

                console.log('좌표 변환 성공:', { latitude, longitude, address });
                resolve({ latitude, longitude });
            } else {
                console.error('좌표 검색 실패:', result);
                alert('좌표를 찾을 수 없습니다.');
                reject(new Error('좌표 검색 실패'));
            }
        };

        // JSONP 요청을 위한 script 태그 생성
        const script = document.createElement('script');
        const params = new URLSearchParams({
            service: 'address',
            request: 'GetCoord',
            version: '2.0',
            crs: 'EPSG:4326',
            type: 'ROAD',
            address: address,
            format: 'json',
            errorformat: 'json',
            key: VWORLD_API_KEY,
            callback: callbackName
        });

        script.src = `https://api.vworld.kr/req/address?${params.toString()}`;
        script.onerror = function() {
            delete window[callbackName];
            document.body.removeChild(script);
            reject(new Error('VWorld API 요청 실패'));
        };

        document.body.appendChild(script);
    });
}

// ============ 주소 저장 ============
async function saveAddress() {
    const zonecode = document.getElementById('zonecode').value;
    const roadAddress = document.getElementById('roadAddress').value;
    const jibunAddress = document.getElementById('jibunAddress').value;
    const detailAddress = document.getElementById('detailAddress').value;
    let latitude = document.getElementById('latitude').value;
    let longitude = document.getElementById('longitude').value;

    if (!roadAddress && !jibunAddress) {
        alert('주소를 검색해주세요.');
        return;
    }

    // 좌표가 없으면 다시 시도
    if (!latitude || !longitude) {
        const fullAddress = roadAddress || jibunAddress;
        try {
            const coords = await getCoordinates(fullAddress);
            latitude = coords.latitude;
            longitude = coords.longitude;
        } catch (error) {
            alert('좌표를 가져올 수 없습니다. 잠시 후 다시 시도해주세요.');
            return;
        }
    }

    // Daum Postcode 데이터에서 시/도, 시/군/구, 동 추출
    const fullAddress = roadAddress || jibunAddress;
    const addressParts = fullAddress.split(' ');

    const addressData = {
        province: addressParts[0] || '',
        city: addressParts[1] || '',
        district: addressParts[2] || '',
        detailAddress: detailAddress || '',
        coordinate: {
            latitude: parseFloat(latitude),
            longitude: parseFloat(longitude)
        }
    };

    console.log('저장할 주소 데이터:', addressData);

    // 주문 확인 시 사용할 주소로 저장
    selectedAddress = addressData;

    alert('주소가 등록되었습니다.');
    closeModal('addressModal');

    // 주문 확인 모달 표시
    showOrderConfirmModal();
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
                await renderProfile();
                break;
            case 'qna':
                await renderQNA();
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

// ============ 사용자 정보 페이지 ============
async function renderProfile() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = '내 정보를 확인하세요';

    if (!currentUser) {
        await loadUserInfo();
    }

    if (!currentUser) {
        contentBody.innerHTML = '<p>사용자 정보를 불러올 수 없습니다.</p>';
        return;
    }

    contentBody.innerHTML = `
        <div style="max-width: 600px;">
            <div style="background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                <h3 style="margin: 0 0 20px 0; padding-bottom: 15px; border-bottom: 2px solid #f0f0f0;">
                    기본 정보
                </h3>
                
                <div style="margin-bottom: 20px;">
                    <label style="display: block; color: #666; font-size: 14px; margin-bottom: 5px;">아이디</label>
                    <p style="margin: 0; font-size: 16px; font-weight: 500;">${currentUser.username}</p>
                </div>
                
                <div style="margin-bottom: 20px;">
                    <label style="display: block; color: #666; font-size: 14px; margin-bottom: 5px;">이메일</label>
                    <p style="margin: 0; font-size: 16px; font-weight: 500;">${currentUser.email}</p>
                </div>
                
                <h3 style="margin: 30px 0 20px 0; padding-bottom: 15px; border-bottom: 2px solid #f0f0f0;">
                    배송지 주소
                </h3>
                
                ${currentUser.addresses && currentUser.addresses.length > 0 ? `
                    <div style="display: grid; gap: 15px;">
                        ${currentUser.addresses.map((addr, index) => `
                            <div style="padding: 15px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef;">
                                <div style="display: flex; justify-content: space-between; align-items: start;">
                                    <div style="flex: 1;">
                                        <p style="margin: 0 0 5px 0; font-weight: 500;">주소 ${index + 1}</p>
                                        <p style="margin: 0; color: #666; font-size: 15px;">${addr.address}</p>
                                        <p style="margin: 5px 0 0 0; color: #999; font-size: 13px;">
                                            위도: ${addr.lat.toFixed(6)}, 경도: ${addr.lon.toFixed(6)}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                ` : '<p style="color: #999;">등록된 배송지가 없습니다.</p>'}
                
                <div style="margin-top: 30px; display: flex; gap: 10px;">
                    <button class="btn btn-primary">정보 수정</button>
                    <button class="btn" onclick="openAddressModal()">주소 추가</button>
                </div>
            </div>
        </div>
    `;
}

function openAddressModal() {
    // 입력 필드 초기화
    document.getElementById('zonecode').value = '';
    document.getElementById('roadAddress').value = '';
    document.getElementById('jibunAddress').value = '';
    document.getElementById('detailAddress').value = '';
    document.getElementById('latitude').value = '';
    document.getElementById('longitude').value = '';

    document.getElementById('addressModal').classList.add('show');
}

// ============ Q&A 페이지 (AI 챗봇 - 히스토리 기반) ============
async function renderQNA() {
    const contentDesc = document.getElementById('contentDesc');
    const contentBody = document.getElementById('contentBody');

    contentDesc.textContent = 'AI에게 궁금한 점을 물어보세요';

    contentBody.innerHTML = `
        <div style="max-width: 800px; height: 600px; display: flex; flex-direction: column; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
            <!-- 채팅 헤더 -->
            <div style="padding: 20px; border-bottom: 1px solid #e0e0e0; background: linear-gradient(135deg, #10b981 0%, #059669 100%); border-radius: 12px 12px 0 0; color: white;">
                <h3 style="margin: 0; display: flex; align-items: center; gap: 10px;">
                    <span style="font-size: 24px;">🤖</span>
                    <span>배달의 만족 AI 도우미</span>
                </h3>
                <p style="margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;">무엇이든 물어보세요!</p>
            </div>
            
            <!-- 채팅 메시지 영역 -->
            <div id="chatMessages" style="flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 15px;">
                <div style="text-align: center; color: #999;">
                    <p style="font-size: 14px;">대화 내역을 불러오는 중...</p>
                </div>
            </div>
            
            <!-- 입력 영역 -->
            <div style="padding: 20px; border-top: 1px solid #e0e0e0;">
                <div style="display: flex; gap: 10px;">
                    <input type="text" id="chatInput" placeholder="메시지를 입력하세요..." 
                           style="flex: 1; padding: 12px 16px; border: 1px solid #ddd; border-radius: 24px; font-size: 15px;"
                           onkeypress="if(event.key === 'Enter') sendQnaMessage()">
                    <button onclick="sendQnaMessage()" class="btn btn-primary" 
                            style="padding: 12px 24px; border-radius: 24px; white-space: nowrap;">
                        전송
                    </button>
                </div>
            </div>
        </div>
    `;

    // QnA 히스토리 로드
    await loadQnaHistory();
}

// QnA 히스토리 로드
async function loadQnaHistory() {
    const chatMessagesDiv = document.getElementById('chatMessages');

    try {
        const response = await fetchAPI(`${API_BASE_URL}/customers/aiprompt/my/qnas`);
        const histories = response.data;

        if (histories.length === 0) {
            chatMessagesDiv.innerHTML = `
                <div style="text-align: center; color: #999; margin-top: 50px;">
                    <p style="font-size: 48px; margin: 0;">💬</p>
                    <p style="margin: 10px 0 0 0;">대화를 시작해보세요!</p>
                    <div style="margin-top: 20px; display: flex; flex-direction: column; gap: 10px; align-items: center;">
                        <button onclick="sendSuggestedQnaQuestion('배달 시간은 얼마나 걸리나요?')" 
                                style="padding: 10px 20px; background: #f0f0f0; border: none; border-radius: 20px; cursor: pointer; transition: background 0.2s;"
                                onmouseover="this.style.background='#e0e0e0'" onmouseout="this.style.background='#f0f0f0'">
                            배달 시간은 얼마나 걸리나요?
                        </button>
                        <button onclick="sendSuggestedQnaQuestion('결제 방법은 어떤게 있나요?')" 
                                style="padding: 10px 20px; background: #f0f0f0; border: none; border-radius: 20px; cursor: pointer; transition: background 0.2s;"
                                onmouseover="this.style.background='#e0e0e0'" onmouseout="this.style.background='#f0f0f0'">
                            결제 방법은 어떤게 있나요?
                        </button>
                        <button onclick="sendSuggestedQnaQuestion('주문 취소는 어떻게 하나요?')" 
                                style="padding: 10px 20px; background: #f0f0f0; border: none; border-radius: 20px; cursor: pointer; transition: background 0.2s;"
                                onmouseover="this.style.background='#e0e0e0'" onmouseout="this.style.background='#f0f0f0'">
                            주문 취소는 어떻게 하나요?
                        </button>
                    </div>
                </div>
            `;
        } else {
            // 히스토리를 채팅 형식으로 렌더링
            renderQnaHistories(histories);
        }

    } catch (error) {
        console.error('QnA 히스토리 로드 실패:', error);
        chatMessagesDiv.innerHTML = `
            <div style="text-align: center; color: #999; margin-top: 50px;">
                <p style="font-size: 48px; margin: 0;">💬</p>
                <p style="margin: 10px 0 0 0;">대화를 시작해보세요!</p>
            </div>
        `;
    }
}

function renderQnaHistories(histories) {
    const chatMessagesDiv = document.getElementById('chatMessages');

    chatMessagesDiv.innerHTML = histories.map(history => `
        <!-- 사용자 질문 -->
        <div style="display: flex; justify-content: flex-end;">
            <div style="max-width: 70%; padding: 12px 16px; border-radius: 16px; 
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white;">
                <p style="margin: 0; line-height: 1.5; white-space: pre-wrap;">${history.requestPrompt}</p>
                <span style="display: block; margin-top: 5px; font-size: 11px; opacity: 0.7;">
                    ${new Date(history.createdAt).toLocaleString()}
                </span>
            </div>
        </div>
        
        <!-- AI 응답 -->
        <div style="display: flex; justify-content: flex-start;">
            <div style="max-width: 70%; padding: 12px 16px; border-radius: 16px; background: #f0f0f0; color: #333;">
                <strong style="display: block; margin-bottom: 5px; color: #10b981;">🤖 AI</strong>
                <p style="margin: 0; line-height: 1.5; white-space: pre-wrap;">${history.responseContent}</p>
                <span style="display: block; margin-top: 5px; font-size: 11px; opacity: 0.7;">
                    ${new Date(history.createdAt).toLocaleString()}
                </span>
            </div>
        </div>
    `).join('');

    scrollQnaChatToBottom();
}

// 제안 질문 전송
function sendSuggestedQnaQuestion(question) {
    document.getElementById('chatInput').value = question;
    sendQnaMessage();
}

// QnA 메시지 전송
async function sendQnaMessage() {
    const input = document.getElementById('chatInput');
    const question = input.value.trim();

    if (!question) return;

    // 입력 필드 초기화 및 비활성화
    input.value = '';
    input.disabled = true;

    // 사용자 메시지 즉시 표시
    const chatMessagesDiv = document.getElementById('chatMessages');

    // 제안 버튼이 있으면 제거
    const suggestions = chatMessagesDiv.querySelector('div[style*="text-align: center"]');
    if (suggestions) {
        suggestions.remove();
    }

    chatMessagesDiv.innerHTML += `
        <div style="display: flex; justify-content: flex-end;">
            <div style="max-width: 70%; padding: 12px 16px; border-radius: 16px; 
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white;">
                <p style="margin: 0; line-height: 1.5; white-space: pre-wrap;">${question}</p>
                <span style="display: block; margin-top: 5px; font-size: 11px; opacity: 0.7;">
                    ${new Date().toLocaleTimeString()}
                </span>
            </div>
        </div>
    `;

    scrollQnaChatToBottom();

    // 타이핑 인디케이터 표시
    showQnaTypingIndicator();

    try {
        // QnA API 호출
        const response = await fetchAPI(`${API_BASE_URL}/customers/aiprompt/qna`, {
            method: 'POST',
            body: JSON.stringify({
                question: question
            })
        });

        const aiResponse = response.data;

        // 타이핑 인디케이터 제거
        hideQnaTypingIndicator();

        // AI 응답 추가
        chatMessagesDiv.innerHTML += `
            <div style="display: flex; justify-content: flex-start;">
                <div style="max-width: 70%; padding: 12px 16px; border-radius: 16px; background: #f0f0f0; color: #333;">
                    <strong style="display: block; margin-bottom: 5px; color: #10b981;">🤖 AI</strong>
                    <p style="margin: 0; line-height: 1.5; white-space: pre-wrap;">${aiResponse.responseContent}</p>
                    <span style="display: block; margin-top: 5px; font-size: 11px; opacity: 0.7;">
                        ${new Date(aiResponse.createdAt).toLocaleString()}
                    </span>
                </div>
            </div>
        `;

        scrollQnaChatToBottom();

    } catch (error) {
        console.error('QnA 전송 오류:', error);

        // 타이핑 인디케이터 제거
        hideQnaTypingIndicator();

        // 오류 메시지 표시
        chatMessagesDiv.innerHTML += `
            <div style="display: flex; justify-content: flex-start;">
                <div style="max-width: 70%; padding: 12px 16px; border-radius: 16px; background: #fee; color: #c00; border: 1px solid #fcc;">
                    <p style="margin: 0; line-height: 1.5;">
                        ❌ 오류가 발생했습니다: ${error.message}
                    </p>
                </div>
            </div>
        `;

        scrollQnaChatToBottom();
    }

    // 입력 필드 활성화
    input.disabled = false;
    input.focus();
}

function showQnaTypingIndicator() {
    const chatMessagesDiv = document.getElementById('chatMessages');
    chatMessagesDiv.innerHTML += `
        <div id="qnaTypingIndicator" style="display: flex; justify-content: flex-start;">
            <div style="padding: 12px 16px; border-radius: 16px; background: #f0f0f0;">
                <div style="display: flex; gap: 4px; align-items: center;">
                    <div style="width: 8px; height: 8px; border-radius: 50%; background: #10b981; animation: typing 1.4s infinite;"></div>
                    <div style="width: 8px; height: 8px; border-radius: 50%; background: #10b981; animation: typing 1.4s infinite 0.2s;"></div>
                    <div style="width: 8px; height: 8px; border-radius: 50%; background: #10b981; animation: typing 1.4s infinite 0.4s;"></div>
                </div>
            </div>
        </div>
        <style>
            @keyframes typing {
                0%, 60%, 100% { transform: translateY(0); opacity: 0.7; }
                30% { transform: translateY(-10px); opacity: 1; }
            }
        </style>
    `;
    scrollQnaChatToBottom();
}

function hideQnaTypingIndicator() {
    const indicator = document.getElementById('qnaTypingIndicator');
    if (indicator) {
        indicator.remove();
    }
}

function scrollQnaChatToBottom() {
    const chatMessagesDiv = document.getElementById('chatMessages');
    chatMessagesDiv.scrollTop = chatMessagesDiv.scrollHeight;
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

        // 리뷰 목록 조회
        let reviews = [];
        try {
            const reviewResponse = await fetchAPI(`${API_BASE_URL}/reviews/restaurant/${restaurantId}?page=0&size=10&sortType=dateDesc`);
            reviews = reviewResponse.data.content || [];
            console.log('리뷰 조회 성공:', reviews);
        } catch (error) {
            console.error('리뷰 조회 실패:', error);
        }

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
            <div style="display: grid; gap: 15px; margin-bottom: 30px;">
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
            
            <!-- 리뷰 섹션 -->
            <div style="border-top: 2px solid #e0e0e0; padding-top: 30px; margin-top: 30px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3 style="margin: 0;">리뷰 (${reviews.length})</h3>
                    ${restaurant.reviewRating ? `
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <span style="font-size: 24px;">⭐</span>
                            <span style="font-size: 20px; font-weight: bold; color: #10b981;">${restaurant.reviewRating}</span>
                            <span style="color: #666;">/ 5.0</span>
                        </div>
                    ` : ''}
                </div>
                
                ${reviews.length > 0 ? `
                    <div style="display: grid; gap: 15px;">
                        ${reviews.map(review => `
                            <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; background: #fafafa;">
                                <!-- 리뷰 헤더 -->
                                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 10px;">
                                    <div>
                                        <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
                                            <span style="font-weight: 500;">${review.reviewerName || '익명'}</span>
                                            <span style="font-size: 14px; color: #10b981;">${'⭐'.repeat(Math.floor(review.rating))}</span>
                                            <span style="font-size: 14px; color: #666;">${review.rating}점</span>
                                        </div>
                                        <p style="margin: 0; font-size: 13px; color: #999;">
                                            ${new Date(review.createdAt).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        })}
                                        </p>
                                    </div>
                                </div>
                                
                                <!-- 주문 메뉴 정보 -->
                                ${review.menus && review.menus.length > 0 ? `
                                    <div style="margin-bottom: 10px; padding: 10px; background: white; border-radius: 4px;">
                                        <p style="margin: 0; font-size: 13px; color: #666;">
                                            주문 메뉴: ${review.menus.map(m => `${m.menuName} x${m.quantity}`).join(', ')}
                                        </p>
                                    </div>
                                ` : ''}
                                
                                <!-- 리뷰 내용 -->
                                <p style="margin: 10px 0 0 0; line-height: 1.6; white-space: pre-wrap;">${review.content}</p>
                            </div>
                        `).join('')}
                    </div>
                    
                    ${reviews.length >= 10 ? `
                        <div style="text-align: center; margin-top: 20px;">
                            <button class="btn" onclick="loadMoreReviews('${restaurantId}', 1)" id="loadMoreReviewsBtn">
                                더보기
                            </button>
                        </div>
                    ` : ''}
                ` : `
                    <div style="text-align: center; padding: 40px 0; color: #999;">
                        <p style="font-size: 48px; margin: 0;">📝</p>
                        <p style="margin: 10px 0 0 0;">아직 작성된 리뷰가 없습니다.</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">첫 번째 리뷰를 남겨보세요!</p>
                    </div>
                `}
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

    // 주소 등록 모달 열기
    openAddressModal();
}

// ============ 주문 확인 모달 ============
function showOrderConfirmModal() {
    if (!selectedAddress) {
        alert('배송지 주소를 등록해주세요.');
        return;
    }

    const totalPrice = cart.reduce((sum, item) => sum + (item.basePrice * item.quantity), 0);

    document.getElementById('orderConfirmBody').innerHTML = `
        <div style="margin-bottom: 20px;">
            <h4 style="margin-bottom: 10px;">주문 상품</h4>
            ${cart.map(item => `
                <div style="padding: 10px; background: #f8f9fa; border-radius: 4px; margin-bottom: 5px;">
                    <p style="margin: 0; font-weight: 500;">${item.menuName} x ${item.quantity}</p>
                    <p style="margin: 5px 0 0 0; color: #666;">${(item.basePrice * item.quantity).toLocaleString()}원</p>
                </div>
            `).join('')}
        </div>
        
        <div style="margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px;">
            <h4 style="margin: 0 0 10px 0;">배송지 정보</h4>
            <p style="margin: 5px 0;">${selectedAddress.province} ${selectedAddress.city} ${selectedAddress.district}</p>
            <p style="margin: 5px 0; color: #666;">${selectedAddress.detailAddress}</p>
            <p style="margin: 5px 0; font-size: 13px; color: #888;">
                좌표: ${selectedAddress.coordinate.latitude.toFixed(6)}, ${selectedAddress.coordinate.longitude.toFixed(6)}
            </p>
        </div>
        
        <div style="margin-bottom: 20px;">
            <h4>총 결제 금액</h4>
            <p style="font-size: 24px; color: #10b981; font-weight: bold; margin: 10px 0;">
                ${totalPrice.toLocaleString()}원
            </p>
        </div>
        
        <div style="margin-bottom: 15px;">
            <label style="display: block; margin-bottom: 5px; font-weight: 500;">전화번호</label>
            <input type="tel" id="orderPhone" placeholder="010-1234-5678" 
                   style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
        </div>
        
        <div style="margin-bottom: 20px;">
            <label style="display: block; margin-bottom: 5px; font-weight: 500;">배달 요청사항</label>
            <textarea id="deliveryRequest" placeholder="예: 문 앞에 놓아주세요" 
                      style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; min-height: 80px;"></textarea>
        </div>
        
        <div style="display: flex; gap: 10px;">
            <button class="btn btn-primary" onclick="confirmAndPay()" style="flex: 1;">
                결제하기
            </button>
            <button class="btn" onclick="closeModal('orderConfirmModal')" style="flex: 1;">
                취소
            </button>
        </div>
    `;

    document.getElementById('orderConfirmModal').classList.add('show');
}

// ============ 결제 진행 ============
async function confirmAndPay() {
    const phone = document.getElementById('orderPhone').value.trim();
    const deliveryRequest = document.getElementById('deliveryRequest').value.trim();

    if (!phone) {
        alert('전화번호를 입력해주세요.');
        return;
    }

    // 전화번호 유효성 검사 (간단한 검사)
    const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
    if (!phoneRegex.test(phone.replace(/-/g, ''))) {
        alert('올바른 전화번호 형식이 아닙니다.');
        return;
    }

    closeModal('orderConfirmModal');

    // 토스 결제 진행
    await initializePaymentForOrder(phone, deliveryRequest);
}

async function initializePaymentForOrder(phone, deliveryRequest) {
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
        totalAmount,
        phone,
        deliveryRequest,
        address: selectedAddress
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
            customerName: currentUser?.username || "사용자",
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

    if (!currentUser) {
        await loadUserInfo();
    }

    try {
        const orderData = {
            orderer: {
                userId: currentUser?.userId || "unknown",
                name: currentUser?.username || "사용자",
                phone: pendingOrder.phone,
                deliveryRequest: pendingOrder.deliveryRequest || "",
                address: pendingOrder.address
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
        selectedAddress = null;
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

        // 각 주문에 대한 리뷰 정보 조회
        const ordersWithReviews = await Promise.all(orders.map(async (order) => {
            let review = null;
            if (order.status === 'COMPLETED') {
                try {
                    // 주문에 대한 리뷰가 있는지 확인
                    const reviewResponse = await fetchAPI(`${API_BASE_URL}/reviews/order/${order.orderId}`);
                    review = reviewResponse.data;
                } catch (error) {
                    // 리뷰가 없으면 에러 발생 (404)
                    console.log('리뷰 없음:', order.orderId);
                }
            }
            return { ...order, review };
        }));

        contentBody.innerHTML = `
            <div style="display: grid; gap: 15px;">
                ${ordersWithReviews.map(order => `
                    <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;" id="order-${order.orderId}">
                        <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 15px;">
                            <div>
                                <h4 style="margin: 0 0 5px 0;">주문번호: ${order.orderId}</h4>
                                <p style="color: #666; font-size: 14px; margin: 0;">
                                    ${new Date(order.createdAt).toLocaleString()}
                                </p>
                            </div>
                            <span style="padding: 5px 10px; background: ${order.status === 'COMPLETED' ? '#d4edda' : '#f0f0f0'}; 
                                         border-radius: 4px; font-size: 14px; color: ${order.status === 'COMPLETED' ? '#155724' : '#333'};">
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
                        
                        <div style="border-top: 1px solid #e0e0e0; padding-top: 15px; margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
                            <p style="font-weight: bold; font-size: 16px; margin: 0;">
                                총 금액: ${order.totalPrice.toLocaleString()}원
                            </p>
                            <div style="display: flex; gap: 10px;">
                                <button class="btn btn-primary" onclick="showOrderDetail('${order.orderId}')">
                                    상세보기
                                </button>
                                ${order.status === 'PENDING' || order.status === 'PAYMENT_COMPLETED' || order.status === 'CONFIRMED' ? `
                                    <button class="btn btn-danger" onclick="showCancelOrderModal('${order.orderId}')">
                                        주문 취소
                                    </button>
                                ` : ''}
                            </div>
                        </div>
                        
                        ${order.status === 'COMPLETED' ? `
                            <!-- 리뷰 영역 -->
                            <div style="border-top: 1px solid #e0e0e0; padding-top: 15px;">
                                ${order.review ? `
                                    <!-- 작성된 리뷰 표시 -->
                                    <div id="review-display-${order.orderId}">
                                        <div style="background: #f8f9fa; padding: 15px; border-radius: 8px;">
                                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                                <h5 style="margin: 0; color: #10b981;">내가 작성한 리뷰</h5>
                                                <div style="display: flex; gap: 5px;">
                                                    <button class="btn" onclick="showEditReviewForm('${order.orderId}', '${order.review.reviewId}', ${order.review.rating}, \`${order.review.content.replace(/`/g, '\\`')}\`)" 
                                                            style="padding: 5px 10px; font-size: 13px;">
                                                        수정
                                                    </button>
                                                    <button class="btn btn-danger" onclick="deleteReview('${order.review.reviewId}', '${order.orderId}')" 
                                                            style="padding: 5px 10px; font-size: 13px;">
                                                        삭제
                                                    </button>
                                                </div>
                                            </div>
                                            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
                                                <span style="font-size: 18px;">${'⭐'.repeat(Math.floor(order.review.rating))}</span>
                                                <span style="color: #666; font-size: 14px;">${order.review.rating}점</span>
                                            </div>
                                            <p style="margin: 0; line-height: 1.6; white-space: pre-wrap;">${order.review.content}</p>
                                            <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">
                                                ${new Date(order.review.createdAt).toLocaleString()}
                                            </p>
                                        </div>
                                    </div>
                                    
                                    <!-- 리뷰 수정 폼 (숨김) -->
                                    <div id="review-edit-${order.orderId}" style="display: none;">
                                        <div style="background: #f8f9fa; padding: 15px; border-radius: 8px;">
                                            <h5 style="margin: 0 0 15px 0;">리뷰 수정</h5>
                                            <div style="margin-bottom: 15px;">
                                                <label style="display: block; margin-bottom: 5px; font-weight: 500;">별점</label>
                                                <div style="display: flex; gap: 5px;" id="rating-edit-${order.orderId}">
                                                    ${[1, 2, 3, 4, 5].map(star => `
                                                        <span class="star-rating" data-rating="${star}" 
                                                              style="font-size: 32px; cursor: pointer; ${star <= order.review.rating ? 'opacity: 1;' : 'opacity: 0.3;'}"
                                                              onclick="setRating('edit-${order.orderId}', ${star})">⭐</span>
                                                    `).join('')}
                                                </div>
                                                <input type="hidden" id="rating-value-edit-${order.orderId}" value="${order.review.rating}">
                                            </div>
                                            <div style="margin-bottom: 15px;">
                                                <label style="display: block; margin-bottom: 5px; font-weight: 500;">리뷰 내용</label>
                                                <textarea id="review-content-edit-${order.orderId}" 
                                                          style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; min-height: 100px; resize: vertical;"
                                                          placeholder="음식과 서비스는 어떠셨나요?">${order.review.content}</textarea>
                                            </div>
                                            <div style="display: flex; gap: 10px;">
                                                <button class="btn btn-primary" onclick="submitEditReview('${order.review.reviewId}', '${order.orderId}')" style="flex: 1;">
                                                    수정 완료
                                                </button>
                                                <button class="btn" onclick="cancelEditReview('${order.orderId}')" style="flex: 1;">
                                                    취소
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ` : `
                                    <!-- 리뷰 작성 버튼 -->
                                    <div id="review-button-${order.orderId}">
                                        <button class="btn btn-primary" onclick="showCreateReviewForm('${order.orderId}', '${order.restaurantId}')" style="width: 100%;">
                                            ⭐ 리뷰 작성하기
                                        </button>
                                    </div>
                                    
                                    <!-- 리뷰 작성 폼 (숨김) -->
                                    <div id="review-form-${order.orderId}" style="display: none;">
                                        <div style="background: #f8f9fa; padding: 15px; border-radius: 8px;">
                                            <h5 style="margin: 0 0 15px 0;">리뷰 작성</h5>
                                            <div style="margin-bottom: 15px;">
                                                <label style="display: block; margin-bottom: 5px; font-weight: 500;">별점</label>
                                                <div style="display: flex; gap: 5px;" id="rating-${order.orderId}">
                                                    ${[1, 2, 3, 4, 5].map(star => `
                                                        <span class="star-rating" data-rating="${star}" 
                                                              style="font-size: 32px; cursor: pointer; opacity: 0.3;"
                                                              onclick="setRating('${order.orderId}', ${star})">⭐</span>
                                                    `).join('')}
                                                </div>
                                                <input type="hidden" id="rating-value-${order.orderId}" value="0">
                                            </div>
                                            <div style="margin-bottom: 15px;">
                                                <label style="display: block; margin-bottom: 5px; font-weight: 500;">리뷰 내용</label>
                                                <textarea id="review-content-${order.orderId}" 
                                                          style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; min-height: 100px; resize: vertical;"
                                                          placeholder="음식과 서비스는 어떠셨나요?"></textarea>
                                            </div>
                                            <div style="display: flex; gap: 10px;">
                                                <button class="btn btn-primary" onclick="submitReview('${order.orderId}', '${order.restaurantId}')" style="flex: 1;">
                                                    리뷰 등록
                                                </button>
                                                <button class="btn" onclick="cancelReview('${order.orderId}')" style="flex: 1;">
                                                    취소
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                `}
                            </div>
                        ` : ''}
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
                <h3 style="color:#10b981; margin-bottom:15px;">결제 완료</h3>
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

// ============ 리뷰 기능 ============

// 별점 설정
function setRating(orderId, rating) {
    // 별점 값 저장
    document.getElementById(`rating-value-${orderId}`).value = rating;

    // 별 표시 업데이트
    const stars = document.querySelectorAll(`#rating-${orderId} .star-rating`);
    stars.forEach(star => {
        const starRating = parseInt(star.getAttribute('data-rating'));
        star.style.opacity = starRating <= rating ? '1' : '0.3';
    });
}

// 리뷰 작성 폼 표시
function showCreateReviewForm(orderId, restaurantId) {
    document.getElementById(`review-button-${orderId}`).style.display = 'none';
    document.getElementById(`review-form-${orderId}`).style.display = 'block';
}

// 리뷰 작성 취소
function cancelReview(orderId) {
    document.getElementById(`review-button-${orderId}`).style.display = 'block';
    document.getElementById(`review-form-${orderId}`).style.display = 'none';

    // 입력 필드 초기화
    document.getElementById(`rating-value-${orderId}`).value = '0';
    document.getElementById(`review-content-${orderId}`).value = '';

    // 별점 초기화
    const stars = document.querySelectorAll(`#rating-${orderId} .star-rating`);
    stars.forEach(star => {
        star.style.opacity = '0.3';
    });
}

// 리뷰 작성 제출
async function submitReview(orderId, restaurantId) {
    const rating = parseFloat(document.getElementById(`rating-value-${orderId}`).value);
    const content = document.getElementById(`review-content-${orderId}`).value.trim();

    if (!currentUser) {
        await loadUserInfo();
    }

    if (rating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    if (!content) {
        alert('리뷰 내용을 입력해주세요.');
        return;
    }

    try {
        // 주문 정보 가져오기
        const orderResponse = await fetchAPI(`${API_BASE_URL}/customers/orders/${orderId}`);
        const order = orderResponse;

        // 메뉴 정보 구성
        const menus = order.items.map(item => ({
            menuId: item.menuId,
            menuName: item.menuName,
            quantity: item.quantity
        }));

        const reviewData = {
            reviewerId: currentUser?.userId || 'unknown',
            orderId: orderId,
            restaurantId: restaurantId,
            menus: menus,
            rating: rating,
            content: content
        };

        console.log('리뷰 작성 데이터:', reviewData);

        const response = await fetchAPI(`${API_BASE_URL}/reviews`, {
            method: 'POST',
            body: JSON.stringify(reviewData)
        });

        alert('리뷰가 등록되었습니다!');

        // 주문 목록 새로고침
        renderOrders();

    } catch (error) {
        console.error('리뷰 작성 오류:', error);
        alert('리뷰 작성에 실패했습니다: ' + error.message);
    }
}

// 리뷰 수정 폼 표시
function showEditReviewForm(orderId, reviewId, rating, content) {
    document.getElementById(`review-display-${orderId}`).style.display = 'none';
    document.getElementById(`review-edit-${orderId}`).style.display = 'block';
}

// 리뷰 수정 취소
function cancelEditReview(orderId) {
    document.getElementById(`review-display-${orderId}`).style.display = 'block';
    document.getElementById(`review-edit-${orderId}`).style.display = 'none';
}

// 리뷰 수정 제출
async function submitEditReview(reviewId, orderId) {
    const rating = parseFloat(document.getElementById(`rating-value-edit-${orderId}`).value);
    const content = document.getElementById(`review-content-edit-${orderId}`).value.trim();

    if (rating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    if (!content) {
        alert('리뷰 내용을 입력해주세요.');
        return;
    }

    try {
        const reviewData = {
            rating: rating,
            content: content
        };

        console.log('리뷰 수정 데이터:', reviewData);

        const response = await fetchAPI(`${API_BASE_URL}/reviews/${reviewId}`, {
            method: 'PUT',
            body: JSON.stringify(reviewData)
        });

        alert('리뷰가 수정되었습니다!');

        // 주문 목록 새로고침
        renderOrders();

    } catch (error) {
        console.error('리뷰 수정 오류:', error);
        alert('리뷰 수정에 실패했습니다: ' + error.message);
    }
}

// 리뷰 삭제
async function deleteReview(reviewId, orderId) {
    if (!confirm('정말 리뷰를 삭제하시겠습니까?')) {
        return;
    }

    try {
        await fetchAPI(`${API_BASE_URL}/reviews/${reviewId}`, {
            method: 'DELETE'
        });

        alert('리뷰가 삭제되었습니다.');

        // 주문 목록 새로고침
        renderOrders();

    } catch (error) {
        console.error('리뷰 삭제 오류:', error);
        alert('리뷰 삭제에 실패했습니다: ' + error.message);
    }
}

// ============ 주문 취소 기능 ============

let currentCancelOrderId = null;
let currentReviewPage = 0;
let currentRestaurantIdForReviews = null;

// 리뷰 더보기
async function loadMoreReviews(restaurantId, page) {
    try {
        const reviewResponse = await fetchAPI(`${API_BASE_URL}/reviews/restaurant/${restaurantId}?page=${page}&size=10&sortType=dateDesc`);
        const newReviews = reviewResponse.data.content || [];

        if (newReviews.length === 0) {
            alert('더 이상 리뷰가 없습니다.');
            return;
        }

        // 기존 리뷰 목록 찾기
        const reviewContainer = document.querySelector('#restaurantDetailBody > div:last-child > div:nth-child(2)');

        // 새 리뷰 추가
        const newReviewsHtml = newReviews.map(review => `
            <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; background: #fafafa;">
                <!-- 리뷰 헤더 -->
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 10px;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
                            <span style="font-weight: 500;">${review.reviewerName || '익명'}</span>
                            <span style="font-size: 14px; color: #10b981;">${'⭐'.repeat(Math.floor(review.rating))}</span>
                            <span style="font-size: 14px; color: #666;">${review.rating}점</span>
                        </div>
                        <p style="margin: 0; font-size: 13px; color: #999;">
                            ${new Date(review.createdAt).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        })}
                        </p>
                    </div>
                </div>
                
                <!-- 주문 메뉴 정보 -->
                ${review.menus && review.menus.length > 0 ? `
                    <div style="margin-bottom: 10px; padding: 10px; background: white; border-radius: 4px;">
                        <p style="margin: 0; font-size: 13px; color: #666;">
                            주문 메뉴: ${review.menus.map(m => `${m.menuName} x${m.quantity}`).join(', ')}
                        </p>
                    </div>
                ` : ''}
                
                <!-- 리뷰 내용 -->
                <p style="margin: 10px 0 0 0; line-height: 1.6; white-space: pre-wrap;">${review.content}</p>
            </div>
        `).join('');

        // 더보기 버튼 앞에 새 리뷰 추가
        const loadMoreBtn = document.getElementById('loadMoreReviewsBtn');
        if (loadMoreBtn) {
            loadMoreBtn.parentElement.insertAdjacentHTML('beforebegin', newReviewsHtml);

            // 마지막 페이지면 더보기 버튼 제거
            if (newReviews.length < 10) {
                loadMoreBtn.parentElement.remove();
            } else {
                // 다음 페이지로 업데이트
                loadMoreBtn.setAttribute('onclick', `loadMoreReviews('${restaurantId}', ${page + 1})`);
            }
        }

        currentReviewPage = page;
        currentRestaurantIdForReviews = restaurantId;

    } catch (error) {
        console.error('리뷰 더보기 오류:', error);
        alert('리뷰를 불러오는데 실패했습니다: ' + error.message);
    }
}

// 주문 취소 모달 표시
function showCancelOrderModal(orderId) {
    currentCancelOrderId = orderId;
    document.getElementById('cancelReason').value = '';
    document.getElementById('cancelOrderModal').classList.add('show');
}

// 주문 취소 제출
async function submitCancelOrder() {
    const cancelReason = document.getElementById('cancelReason').value.trim();

    if (!cancelReason) {
        alert('취소 사유를 입력해주세요.');
        return;
    }

    if (!currentCancelOrderId) {
        alert('주문 정보를 찾을 수 없습니다.');
        return;
    }

    try {
        const cancelData = {
            cancelReason: cancelReason
        };

        console.log('주문 취소 데이터:', cancelData);

        await fetchAPI(`${API_BASE_URL}/customers/orders/${currentCancelOrderId}/cancel`, {
            method: 'POST',
            body: JSON.stringify(cancelData)
        });

        alert('주문이 취소되었습니다.');

        // 모달 닫기
        closeModal('cancelOrderModal');
        currentCancelOrderId = null;

        // 주문 목록 새로고침
        renderOrders();

    } catch (error) {
        console.error('주문 취소 오류:', error);
        alert('주문 취소에 실패했습니다: ' + error.message);
    }
}

// ============ 페이지 로드 ============
const token = getToken();

if (!token) {
    window.location.href = "/view/client/login";
} else {
    renderMainLayout();
}