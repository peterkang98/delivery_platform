# 즐겨찾기(Favorites) 기능 구현 이슈

## 1. 기능 개요 (Summary)

사용자가 메뉴 또는 음식점을 즐겨찾기(찜)할 수 있는 기능을 구현한다. 또한, 사용자 본인의 즐겨찾기 목록 조회 및 관리자용 통계 조회 기능을 제공한다.

### 주요 기능

- 메뉴 / 레스토랑 즐겨찾기 토글 (등록 / 해제)
- 사용자 본인의 즐겨찾기 목록 조회 (`/v1/favorites/me`)
- 관리자용 즐겨찾기 통계 조회 API 제공

---

## 2. 상세 설명 (Details)

### 2.1 도메인 모델

**Favorite** (즐겨찾기)

- 사용자와 대상(메뉴 또는 레스토랑) 간의 즐겨찾기 관계를 나타내는 도메인 모델

| 필드 | 설명 |
| --- | --- |
| id | 즐겨찾기 식별자 (AUTO_INCREMENT) |
| customerId | 사용자 ID (구매자 기준) |
| targetType | 찜 대상 구분 (`MENU` 또는 `RESTAURANT`) |
| targetId | 메뉴 ID 또는 레스토랑 ID |
| createdAt | 생성 시각 |

**Enum: FavoriteType**

- `MENU`
- `RESTAURANT`

---

### 2.2 인프라 계층

| 클래스 | 역할 |
| --- | --- |
| FavoriteEntity | JPA 엔티티 |
| FavoriteJpaRepository | Spring Data JPA 기본 조회/저장 |
| FavoriteRepository | 도메인 관점의 인터페이스 |
| FavoriteRepositoryImpl | 엔티티 ↔ 도메인 매핑 및 QueryDSL 구현 |

**테이블 스키마**

| 구분 | 컬럼 | 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| PK | id | BIGINT | AUTO_INCREMENT | 즐겨찾기 식별자 |
| FK | customer_id | BIGINT | NOT NULL | 사용자 ID |
| FK | menu_id | BIGINT | NULL | FavoriteType = MENU 일 때 |
| FK | restaurant_id | BIGINT | NULL | FavoriteType = RESTAURANT 일 때 |
|  | type | ENUM('MENU','RESTAURANT') | NOT NULL | 대상 구분 |
|  | created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |

---

### 2.3 애플리케이션 서비스

| 서비스 | 설명 |
| --- | --- |
| FavoriteCommandService | 즐겨찾기 등록/해제 토글 처리 |
| FavoriteQueryService | 즐겨찾기 목록 조회 및 통계 조회 |

**핵심 메서드**

- `toggleFavorite(customerId, targetType, targetId)`
- `getFavoritesByCustomer(customerId)` → `/me` 에서 사용
- `getFavoriteStats()` → 관리자용 통계

---

### 2.4 Presentation (API)

### 사용자 API

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/v1/favorites` | 메뉴/레스토랑 즐겨찾기 등록 및 해제 (토글) |
| GET | `/v1/favorites/me` | 로그인된 사용자 본인 즐겨찾기 조회 |

### 관리자용 API

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/v1/admin/favorites` | 전체 즐겨찾기 목록 조회 (필터링 가능) |
| GET | `/v1/admin/favorites/stats` | 전체 즐겨찾기 통계 조회 |

**관리자용 통계 예시 응답**

```json
{
  "totalFavorites": 1200,
  "menuFavorites": 900,
  "restaurantFavorites": 300,
  "topFavoriteMenus": [ { menuId, count } ],
  "topFavoriteRestaurants": [ { restaurantId, count } ]
}

```

---

## 3. 테스트 시나리오 (Test Scenario)

### 3.1 Command Service 테스트

- 새로운 즐겨찾기 등록 시 DB에 레코드 생성
- 동일 대상 요청 시 즐겨찾기 해제 (삭제 또는 상태 토글)
- 중복 등록 방지 검증

### 3.2 Query Service 테스트

- `/me` 호출 시 해당 사용자의 즐겨찾기만 반환되는지 검증
- 즐겨찾기 비어있을 경우 빈 리스트 반환 확인

### 3.3 Admin Stats 테스트

- 전체 즐겨찾기 개수 조회 정확성 확인
- 메뉴 / 레스토랑 유형별 비율 조회 검증
- 상위 N개 인기 메뉴/레스토랑 집계 테스트

---

## 4. 참고 자료 (References)

- 관련 이슈: #
- 관련 PR: #
- 유사 서비스 사례 (배달의민족 찜하기, 쿠팡이츠 즐겨찾기)
- DDD / CQRS 적용 기준 문서