<html>
<body>
<!--StartFragment--><h1>주문(Order) 기능 구현 이슈</h1>
<h2>1. 기능 개요 (Summary)</h2>
<p>사용자가 레스토랑의 메뉴를 주문하고 결제/상태 변경을 진행하는 <strong>주문 기능</strong>을 구현한다. 주문은 생성 시점의 메뉴/옵션/가격 정보를 스냅샷으로 보관하며, 주문 상태 흐름(결제 대기 → 결제 완료 → 접수 대기 → 조리중 → 배달중 → 완료 / 취소)을 지원한다. 주문 데이터는 Aggregate Root인 <strong>Order</strong> 가 전 생명주기를 관리하며 하위 구조(OrderItem, OptionGroup, Option)는 값 객체 컬렉션으로 보관한다.</p>
<p><strong>주요 기능</strong></p>
<ul>
<li>주문 생성 (메뉴/옵션 스냅샷 저장)</li>
<li>주문 상세 조회 / 사용자 본인 주문 목록 조회</li>
<li>주문 상태 변경 (접수, 조리중, 배달중, 완료, 취소)</li>
<li>결제 연동(간단: paymentId + isPaid 플래그)</li>
<li>감사/소프트삭제 필드 관리</li>
</ul>
<hr>
<h2>2. 상세 설명 (Details)</h2>
<h3>2.1 도메인 모델</h3>
<h3>Order (Aggregate Root)</h3>

필드 | 타입 | 설명
-- | -- | --
id | String | 주문 ID (UUID)
orderer | Orderer | 주문자 스냅샷 (이름/전화/주소)
items | List | 주문 품목 목록 (VO 컬렉션)
status | OrderStatus | 주문 상태 Enum
payment | Payment | 결제 정보(간단)
orderPrice | BigDecimal | 총 주문 금액(스냅샷 계산값)
requestedAt | LocalDateTime | 주문 생성 시각
confirmedAt | LocalDateTime? | 가게 접수 시각
completedAt | LocalDateTime? | 배달 완료 시각
canceledAt | LocalDateTime? | 취소 시각
cancelReason | String? | 취소 사유
createdAt/By | DateTime/String | 감사 필드
updatedAt/By | DateTime/String | 감사 필드
isDeleted | boolean | 소프트 삭제 여부
deletedAt/By | DateTime/String | 소프트 삭제 정보


<p><strong>주문 생성 요청 예시</strong></p>
<pre><code class="language-json">{
  &quot;orderer&quot;: {
    &quot;userId&quot;: &quot;user-123&quot;,
    &quot;name&quot;: &quot;홍길동&quot;,
    &quot;phone&quot;: &quot;010-1234-5678&quot;,
    &quot;address&quot;: {
      &quot;province&quot;: &quot;서울특별시&quot;,
      &quot;city&quot;: &quot;종로구&quot;,
      &quot;district&quot;: &quot;청운효자동&quot;,
      &quot;detailAddress&quot;: &quot;세종대로 1&quot;,
      &quot;coordinate&quot;: { &quot;latitude&quot;: 37.5759, &quot;longitude&quot;: 126.9768 }
    }
  },
  &quot;items&quot;: [
    {
      &quot;menuId&quot;: &quot;menu-1&quot;,
      &quot;restaurantId&quot;: &quot;rest-9&quot;,
      &quot;menuName&quot;: &quot;불고기 정식&quot;,
      &quot;basePrice&quot;: 12000,
      &quot;quantity&quot;: 2,
      &quot;optionGroups&quot;: [
        {
          &quot;groupName&quot;: &quot;사이즈&quot;,
          &quot;options&quot;: [ { &quot;optionName&quot;: &quot;Large&quot;, &quot;additionalPrice&quot;: 2000, &quot;quantity&quot;: 1 } ]
        }
      ]
    }
  ]
}

</code></pre>
<p><strong>주문 조회 응답 예시(요약)</strong></p>
<pre><code class="language-json">{
  &quot;orderId&quot;: &quot;ord-abc&quot;,
  &quot;status&quot;: &quot;PAID&quot;,
  &quot;orderPrice&quot;: 26000,
  &quot;requestedAt&quot;: &quot;2025-11-05T10:12:00&quot;,
  &quot;items&quot;: [
    {
      &quot;menuId&quot;: &quot;menu-1&quot;,
      &quot;menuName&quot;: &quot;불고기 정식&quot;,
      &quot;basePrice&quot;: 12000,
      &quot;quantity&quot;: 2,
      &quot;optionGroups&quot;: [
        { &quot;groupName&quot;: &quot;사이즈&quot;, &quot;options&quot;: [ { &quot;optionName&quot;: &quot;Large&quot;, &quot;additionalPrice&quot;: 2000, &quot;quantity&quot;: 1 } ] }
      ]
    }
  ],
  &quot;payment&quot;: { &quot;paymentId&quot;: &quot;pay-xyz&quot;, &quot;isPaid&quot;: true }
}

</code></pre>
<hr>
<h2>3. 테스트 시나리오 (Test Scenario)</h2>
<h3>3.1 Command Service</h3>
<ul>
<li>주문 생성 시:
<ul>
<li>Order/OrderItem/OptionGroup/Option 스냅샷이 모두 저장된다</li>
<li>총 금액 계산이 올바르다</li>
</ul>
</li>
<li>결제 완료 표시 시:
<ul>
<li>paymentId 저장 및 isPaid = true 가 된다</li>
<li>상태가 <code>PAYMENT_PENDING</code> → <code>PAID</code> 로 변경된다(정책은 후속 이슈에서 확정)</li>
</ul>
</li>
<li>상태 변경(PAID → WAITING_ACCEPT → COOKING → DELIVERING → DELIVERED) 흐름이 정상 동작한다</li>
<li>취소 시:
<ul>
<li>상태가 <code>CANCELLED</code> 로 변경되고 <code>canceledAt</code>, <code>cancelReason</code> 이 저장된다</li>
</ul>
</li>
</ul>
<h3>3.2 Query Service</h3>
<ul>
<li>/me 호출 시 로그인 사용자의 주문만 페이징 반환</li>
<li>단건 조회 시 전체 스냅샷(메뉴/옵션/주소/결제)이 정확히 반환</li>
<li>삭제된 주문(isDeleted=true)은 기본 조회에서 제외</li>
</ul>
<h3>3.3 Persistence (매핑)</h3>
<ul>
<li><code>@ElementCollection</code> 기반 컬렉션이 정상적으로 저장/조회된다</li>
<li>Address/Coordinate 임베디드 필드가 올바르게 매핑된다</li>
<li>감사/소프트삭제 필드 자동 세팅 확인</li>
</ul>
<hr>
<h2>4. 참고 자료 (References)</h2>
<ul>
<li>관련 이슈: #</li>
<li>관련 PR: #</li>
<li>유사 서비스 사례: 배달앱 주문 플로우 (배민/쿠팡이츠)</li>
<li>DDD / CQRS 적용 기준 문서</li>
</ul>
<!-- notionvc: d8bd0c9d-e42c-4417-91af-8a89c7aaa858 --><!--EndFragment-->
</body>
</html># 주문(Order) 기능 구현 이슈

## 1. 기능 개요 (Summary)

사용자가 레스토랑의 메뉴를 주문하고 결제/상태 변경을 진행하는 **주문 기능**을 구현한다. 주문은 생성 시점의 메뉴/옵션/가격 정보를 스냅샷으로 보관하며, 주문 상태 흐름(결제 대기 → 결제 완료 → 접수 대기 → 조리중 → 배달중 → 완료 / 취소)을 지원한다. 주문 데이터는 Aggregate Root인 **Order** 가 전 생명주기를 관리하며 하위 구조(OrderItem, OptionGroup, Option)는 값 객체 컬렉션으로 보관한다.

**주요 기능**

- 주문 생성 (메뉴/옵션 스냅샷 저장)
- 주문 상세 조회 / 사용자 본인 주문 목록 조회
- 주문 상태 변경 (접수, 조리중, 배달중, 완료, 취소)
- 결제 연동(간단: paymentId + isPaid 플래그)
- 감사/소프트삭제 필드 관리

---

## 2. 상세 설명 (Details)

### 2.1 도메인 모델

### Order (Aggregate Root)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | String | 주문 ID (UUID) |
| orderer | Orderer | 주문자 스냅샷 (이름/전화/주소) |
| items | List | 주문 품목 목록 (VO 컬렉션) |
| status | OrderStatus | 주문 상태 Enum |
| payment | Payment | 결제 정보(간단) |
| orderPrice | BigDecimal | 총 주문 금액(스냅샷 계산값) |
| requestedAt | LocalDateTime | 주문 생성 시각 |
| confirmedAt | LocalDateTime? | 가게 접수 시각 |
| completedAt | LocalDateTime? | 배달 완료 시각 |
| canceledAt | LocalDateTime? | 취소 시각 |
| cancelReason | String? | 취소 사유 |
| createdAt/By | DateTime/String | 감사 필드 |
| updatedAt/By | DateTime/String | 감사 필드 |
| isDeleted | boolean | 소프트 삭제 여부 |
| deletedAt/By | DateTime/String | 소프트 삭제 정보 |

### Orderer (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| userId | String | 주문자 ID |
| name | String | 주문자 이름 |
| phone | String | 연락처 |
| address | Address | 배송지 |

### Address (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| province | String | 시/도 |
| city | String | 시/군/구 |
| district | String | 동/읍/면 |
| detailAddress | String | 상세주소 |
| coordinate | Coordinate | 위/경도 |

### Coordinate (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| latitude | BigDecimal | 위도 (-90 ~ 90) |
| longitude | BigDecimal | 경도 (-180 ~ 180) |

### OrderItem (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| menuId | String | 메뉴 ID (스냅샷 기준) |
| restaurantId | String | 레스토랑 ID |
| menuName | String | 메뉴명 스냅샷 |
| basePrice | BigDecimal | 기본 가격 스냅샷 |
| quantity | Integer | 수량 |
| optionGroups | List | 선택된 옵션 그룹 목록 |
| **itemTotalPrice** | BigDecimal | **(저장)** 해당 품목 총 금액 = (기본가격 + 옵션합) × 수량 |

> itemTotalPrice 는 조회/정산/취소/리오더에서 자주 활용되므로 스냅샷으로 저장한다.
>

### 가격 계산 규칙

```
itemTotalPrice = (basePrice + Σ(optionGroup.calculateTotalPrice())) × quantity

```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| menuId | String | 메뉴 ID (스냅샷 기준) |
| restaurantId | String | 레스토랑 ID |
| menuName | String | 메뉴명 스냅샷 |
| basePrice | BigDecimal | 기본 가격 스냅샷 |
| quantity | Integer | 수량 |
| optionGroups | List | 선택된 옵션 그룹 목록 |

### OrderOptionGroup (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| groupName | String | 옵션 그룹명 (예: 사이즈) |
| options | List | 옵션 목록 |

### OrderOption (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| optionName | String | 옵션명 (예: Large) |
| description | String | 옵션 설명 (선택) |
| additionalPrice | BigDecimal | 추가 금액 |
| quantity | Integer | 옵션 수량 (예: 치즈 2개) |

### Payment (Value Object)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| paymentId | String | PG/내부 결제 트랜잭션 ID |
| isPaid | Boolean | 결제 완료 여부 |

### OrderStatus (Enum)

- PAYMENT_PENDING (결제 대기)
- PAID (결제 완료)
- WAITING_ACCEPT (가게 확인 전)
- COOKING (조리중)
- DELIVERING (배달중)
- DELIVERED (배달 완료)
- CANCELLED (주문 취소)

> 정책/행동은 후속 이슈에서 정의. 본 이슈는 데이터 구조 중심.
>

---

### 2.2 인프라 계층

**클래스 / 역할**

- `OrderEntity` : JPA 엔티티 (Aggregate Root)
- `OrderJpaRepository` : Spring Data JPA 기본 저장/조회
- `OrderRepository` : 도메인 관점 인터페이스
- `OrderRepositoryImpl` : 엔티티 ↔ 도메인 매핑, QueryDSL 커스텀 조회
- `@ElementCollection` 을 활용하여 `OrderItem`, `OrderOptionGroup`, `OrderOption`, `Address/Coordinate` 를 임베디드/컬렉션으로 저장 (변경시 전체 재기록 허용)

**테이블 스키마 (초안)**

- `orders`
    - `id` (PK, VARCHAR(36))
    - `user_id` (VARCHAR) — Orderer.userId
    - `orderer_name` (VARCHAR)
    - `orderer_phone` (VARCHAR)
    - `addr_province` (VARCHAR)
    - `addr_city` (VARCHAR)
    - `addr_district` (VARCHAR)
    - `addr_detail` (VARCHAR)
    - `coord_latitude` (DECIMAL(10,7))
    - `coord_longitude` (DECIMAL(10,7))
    - `status` (ENUM)
    - `payment_id` (VARCHAR)
    - `is_paid` (BOOLEAN)
    - `order_price` (DECIMAL(15,2))
    - `requested_at` (DATETIME)
    - `confirmed_at` (DATETIME NULL)
    - `completed_at` (DATETIME NULL)
    - `canceled_at` (DATETIME NULL)
    - `cancel_reason` (VARCHAR NULL)
    - `created_at/by`, `updated_at/by`, `is_deleted`, `deleted_at/by`
- `order_items` (ElementCollection)
    - `order_id` (FK → [orders.id](http://orders.id/))
    - `line_no` (INT) — 아이템 순번 (PK 구성)
    - `menu_id` (VARCHAR)
    - `restaurant_id` (VARCHAR)
    - `menu_name` (VARCHAR)
    - `base_price` (DECIMAL(15,2))
    - `quantity` (INT)
    - **PK**: (`order_id`, `line_no`)
- `order_item_option_groups` (ElementCollection)
    - `order_id` (FK)
    - `line_no` (FK → order_items.line_no)
    - `group_no` (INT) — 그룹 순번 (PK 구성)
    - `group_name` (VARCHAR)
    - **PK**: (`order_id`, `line_no`, `group_no`)
- `order_item_options` (ElementCollection)
    - `order_id` (FK)
    - `line_no` (FK)
    - `group_no` (FK)
    - `option_no` (INT) — 옵션 순번 (PK 구성)
    - `option_name` (VARCHAR)
    - `description` (VARCHAR NULL)
    - `additional_price` (DECIMAL(15,2))
    - `quantity` (INT)
    - **PK**: (`order_id`, `line_no`, `group_no`, `option_no`)

> 구현 편의상 line_no, group_no, option_no 등 순번 기반 PK 사용. 값 객체 컬렉션과 잘 맞는다.
>

---

### 2.3 애플리케이션 서비스

**서비스 / 역할**

- `OrderCommandService`
    - 주문 생성, 취소, 상태 변경(접수, 조리중, 배달중, 완료)
    - 결제 완료 표시(간단 결제 연동)
- `OrderQueryService`
    - 주문 상세 조회, 사용자 본인 주문 목록 조회, 상태별 목록(선택)

**핵심 메서드 (초안)**

- `createOrder(CreateOrderRequest req) : OrderId`
- `getOrder(String orderId) : OrderDetail`
- `getMyOrders(String userId, Pageable) : Page<OrderSummary>`
- `markPaid(String orderId, String paymentId)`
- `accept(String orderId)` / `startCooking(...)` / `startDelivery(...)` / `completeDelivery(...)`
- `cancel(String orderId, String reason)`

---

### 2.4 Presentation (API)

**사용자 API**

| Method | Path | 설명 |
| --- | --- | --- |
| POST | /v1/orders | 주문 생성 (바디에 메뉴/옵션/주소 포함) |
| GET | /v1/orders/{orderId} | 주문 단건 조회 |
| GET | /v1/orders/me | 로그인 사용자 본인 주문 목록 |

**점주/관리자 API**

| Method | Path | 설명 |
| --- | --- | --- |
| PATCH | /v1/admin/orders/{orderId}/accept | 주문 접수 |
| PATCH | /v1/admin/orders/{orderId}/cooking | 조리 시작 |
| PATCH | /v1/admin/orders/{orderId}/delivering | 배달 시작 |
| PATCH | /v1/admin/orders/{orderId}/complete | 배달 완료 |
| PATCH | /v1/admin/orders/{orderId}/cancel | 주문 취소 |

**주문 생성 요청 예시**

```json
{
  "orderer": {
    "userId": "user-123",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "address": {
      "province": "서울특별시",
      "city": "종로구",
      "district": "청운효자동",
      "detailAddress": "세종대로 1",
      "coordinate": { "latitude": 37.5759, "longitude": 126.9768 }
    }
  },
  "items": [
    {
      "menuId": "menu-1",
      "restaurantId": "rest-9",
      "menuName": "불고기 정식",
      "basePrice": 12000,
      "quantity": 2,
      "optionGroups": [
        {
          "groupName": "사이즈",
          "options": [ { "optionName": "Large", "additionalPrice": 2000, "quantity": 1 } ]
        }
      ]
    }
  ]
}

```

**주문 조회 응답 예시(요약)**

```json
{
  "orderId": "ord-abc",
  "status": "PAID",
  "orderPrice": 26000,
  "requestedAt": "2025-11-05T10:12:00",
  "items": [
    {
      "menuId": "menu-1",
      "menuName": "불고기 정식",
      "basePrice": 12000,
      "quantity": 2,
      "optionGroups": [
        { "groupName": "사이즈", "options": [ { "optionName": "Large", "additionalPrice": 2000, "quantity": 1 } ] }
      ]
    }
  ],
  "payment": { "paymentId": "pay-xyz", "isPaid": true }
}

```

---

## 3. 테스트 시나리오 (Test Scenario)

### 3.1 Command Service

- 주문 생성 시:
    - Order/OrderItem/OptionGroup/Option 스냅샷이 모두 저장된다
    - 총 금액 계산이 올바르다
- 결제 완료 표시 시:
    - paymentId 저장 및 isPaid = true 가 된다
    - 상태가 `PAYMENT_PENDING` → `PAID` 로 변경된다(정책은 후속 이슈에서 확정)
- 상태 변경(PAID → WAITING_ACCEPT → COOKING → DELIVERING → DELIVERED) 흐름이 정상 동작한다
- 취소 시:
    - 상태가 `CANCELLED` 로 변경되고 `canceledAt`, `cancelReason` 이 저장된다

### 3.2 Query Service

- /me 호출 시 로그인 사용자의 주문만 페이징 반환
- 단건 조회 시 전체 스냅샷(메뉴/옵션/주소/결제)이 정확히 반환
- 삭제된 주문(isDeleted=true)은 기본 조회에서 제외

### 3.3 Persistence (매핑)

- `@ElementCollection` 기반 컬렉션이 정상적으로 저장/조회된다
- Address/Coordinate 임베디드 필드가 올바르게 매핑된다
- 감사/소프트삭제 필드 자동 세팅 확인

---

## 4. 참고 자료 (References)

- 관련 이슈: #
- 관련 PR: #
- 유사 서비스 사례: 배달앱 주문 플로우 (배민/쿠팡이츠)
- DDD / CQRS 적용 기준 문서