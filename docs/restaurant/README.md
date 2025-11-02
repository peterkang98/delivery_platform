# 1. 도메인 개요

배달 플랫폼의 **Restaurant(음식점) 도메인**을 구현합니다.

**도메인 구조:**
- **Restaurant**: Aggregate Root
- **Menu**: Entity (Restaurant 도메인 내)
- **MenuOption**: Value Object
- **OperatingDay**: Entity
- **RestaurantKeyword**: Entity

**핵심 원칙:**
- DDD Aggregate 패턴 적용
- 모든 Menu 생성/수정은 Restaurant를 통해서만 가능
- 권한별 API 엔드포인트 분리 (Customer, Owner, Manager)
- Soft Delete 적용 (is_deleted, deleted_at)
- UUID 기반 식별자 사용

---

# 2. 기능 목록 (API)

## 2.1 고객용 API (Customer)

**Base Path:** `/v1/customers/restaurants`

### Restaurant 조회
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| GET | `/v1/customers/restaurants` | 식당 목록 조회 (필터링) | 🔓 Public |
| GET | `/v1/customers/restaurants/{restaurantId}` | 특정 식당 상세 조회 | 🔓 Public |

**필터링 파라미터:**
- `?province=서울특별시&city=종로구&district=광화문동` (지역별)
- `?category=한식` (카테고리별)
- `?keyword=김치찌개` (키워드 검색)

### Menu 조회
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| GET | `/v1/customers/restaurants/{restaurantId}/menus` | 특정 식당의 메뉴 목록 조회 | 🔓 Public |
| GET | `/v1/customers/restaurants/{restaurantId}/menus/{menuId}` | 특정 메뉴 상세 조회 | 🔓 Public |

**조회 조건:**
- `is_deleted = false` (삭제되지 않은 것만)
- `is_hidden = false` (숨김 처리되지 않은 것만)
- `is_available = true` (판매 가능한 것만)

---

## 2.2 판매자용 API (Owner)

**Base Path:** `/v1/owners/restaurants`

### Restaurant 관리
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| POST | `/v1/owners/restaurants` | 식당 등록 | 🔒 OWNER |
| GET | `/v1/owners/restaurants` | 내 식당 목록 조회 | 🔒 OWNER |
| GET | `/v1/owners/restaurants/{restaurantId}` | 내 식당 상세 조회 | 🔒 OWNER |
| PUT | `/v1/owners/restaurants/{restaurantId}` | 식당 정보 전체 수정 | 🔒 OWNER |
| PATCH | `/v1/owners/restaurants/{restaurantId}` | 식당 정보 부분 수정 | 🔒 OWNER |
| DELETE | `/v1/owners/restaurants/{restaurantId}` | 식당 삭제 (Soft Delete) | 🔒 OWNER |

**권한 검증:**
- `owner_id = 현재 로그인 사용자 ID`

### Menu 관리
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| POST | `/v1/owners/restaurants/{restaurantId}/menus` | 메뉴 등록 | 🔒 OWNER |
| GET | `/v1/owners/restaurants/{restaurantId}/menus` | 내 식당 메뉴 목록 조회 | 🔒 OWNER |
| GET | `/v1/owners/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 상세 조회 | 🔒 OWNER |
| PUT | `/v1/owners/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 전체 수정 | 🔒 OWNER |
| PATCH | `/v1/owners/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 부분 수정 | 🔒 OWNER |
| PATCH | `/v1/owners/restaurants/{restaurantId}/menus/{menuId}/hide` | 메뉴 숨김/노출 처리 | 🔒 OWNER |
| DELETE | `/v1/owners/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 삭제 (Soft Delete) | 🔒 OWNER |

**조회 조건:**
- 삭제된 메뉴도 조회 가능 (`is_deleted = true` 포함)
- 숨김 처리된 메뉴도 조회 가능 (`is_hidden = true` 포함)

---

## 2.3 관리자용 API (Manager/Master)

**Base Path:** `/v1/admin/restaurants`

### Restaurant 관리
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| GET | `/v1/admin/restaurants` | 전체 식당 목록 조회 (관리용) | 🔒 MANAGER |
| GET | `/v1/admin/restaurants/{restaurantId}` | 특정 식당 상세 조회 | 🔒 MANAGER |
| PUT | `/v1/admin/restaurants/{restaurantId}` | 식당 정보 수정 | 🔒 MANAGER |
| PATCH | `/v1/admin/restaurants/{restaurantId}/status` | 식당 상태 변경 (OPEN/CLOSED) | 🔒 MANAGER |
| DELETE | `/v1/admin/restaurants/{restaurantId}` | 식당 삭제 (Soft Delete) | 🔒 MANAGER |
| PATCH | `/v1/admin/restaurants/{restaurantId}/restore` | 삭제된 식당 복구 | 🔒 MANAGER |

**조회 조건:**
- 삭제된 식당도 조회 가능 (`is_deleted = true` 포함)
- 모든 식당 접근 가능 (owner_id 무관)

### Menu 관리
| Method | Endpoint | 기능 | 권한 |
|--------|----------|------|------|
| GET | `/v1/admin/restaurants/{restaurantId}/menus` | 특정 식당의 전체 메뉴 조회 | 🔒 MANAGER |
| PUT | `/v1/admin/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 정보 수정 | 🔒 MANAGER |
| DELETE | `/v1/admin/restaurants/{restaurantId}/menus/{menuId}` | 메뉴 삭제 (Soft Delete) | 🔒 MANAGER |
| PATCH | `/v1/admin/restaurants/{restaurantId}/menus/{menuId}/restore` | 삭제된 메뉴 복구 | 🔒 MANAGER |

**조회 조건:**
- 삭제된 메뉴도 조회 가능
- 모든 메뉴 접근 가능

---

## 2.4 주요 기능 동작

### Restaurant 생성
```
POST /v1/owners/restaurants
→ UUID 생성
→ owner_id에 현재 로그인 사용자 ID 설정
→ created_by 기록
→ 초기 상태: OPEN, is_active=true, is_deleted=false
```

### Menu 생성
```
POST /v1/owners/restaurants/{restaurantId}/menus
→ Restaurant 소유 권한 검증 (owner_id == 현재 사용자)
→ Restaurant 상태 확인 (CLOSED면 생성 불가)
→ UUID 생성
→ created_by 기록
→ 초기 상태: is_available=true, is_hidden=false, is_deleted=false
```

### Restaurant 삭제 (Soft Delete)
```
DELETE /v1/owners/restaurants/{restaurantId}
→ is_deleted = true
→ deleted_at = 현재 시간
→ deleted_by = 현재 사용자 ID
→ 관련 Menu들도 cascade soft delete
```

### Menu 숨김 처리
```
PATCH /v1/owners/restaurants/{restaurantId}/menus/{menuId}/hide
→ is_hidden = true (고객에게 노출 안됨)
→ is_deleted는 여전히 false (삭제는 아님)
→ updated_by 기록
```

### 권한 검증 로직
```
OWNER:
- 자신의 식당(owner_id == userId)만 수정/삭제 가능
- 자신의 식당 메뉴만 관리 가능

MANAGER/MASTER:
- 모든 식당/메뉴 접근 가능
- 삭제된 데이터도 조회/복구 가능

CUSTOMER:
- 조회만 가능
- is_deleted=false, is_hidden=false인 것만 조회
```

---

## 2.5 공통 사항

### 필터링 및 검색
- 지역별: `province`, `city`, `district`
- 카테고리: `category` (한식, 중식, 분식, 치킨, 피자)
- 키워드: `restaurant_name`, `tags`, `p_restaurant_keyword` 활용
- 초기에는 광화문 근처로 제한

### Soft Delete
- 삭제 시: `is_deleted=true`, `deleted_at=NOW()`, `deleted_by=userId`
- 조회 시: 기본적으로 `is_deleted=false`만 조회
- 관리자는 삭제된 데이터도 조회 가능

### Audit
- 모든 생성: `created_by` 기록
- 모든 수정: `updated_by` 기록
- 모든 삭제: `deleted_by` 기록