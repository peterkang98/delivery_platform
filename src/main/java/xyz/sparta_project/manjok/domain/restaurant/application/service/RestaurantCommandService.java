package xyz.sparta_project.manjok.domain.restaurant.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.response.AdminRestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request.AdminRestaurantUpdateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantCreateRequest;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.response.RestaurantResponse;
import xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request.RestaurantUpdateRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Restaurant Command Service
 * - Restaurant 생성, 수정, 삭제 등 CUD 작업 담당
 * - DDD Aggregate 패턴: Restaurant를 통해서만 하위 엔티티 조작
 * - 트랜잭션 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantCommandService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final RestaurantMapper restaurantMapper;

    /**
     * 식당 등록 (Owner)
     * - UUID 기반 ID 생성
     * - 초기 상태: OPEN, isActive=true
     * - 주소 및 좌표 검증
     */
    public RestaurantResponse createRestaurant(String ownerId, String ownerName,
                                               RestaurantCreateRequest request) {
        log.info("식당 등록 시작 - ownerId: {}, restaurantName: {}", ownerId, request.getRestaurantName());

        // 1. 중복 검증 (동일 Owner의 동일 이름 식당)
        if (restaurantRepository.existsByOwnerIdAndName(ownerId, request.getRestaurantName())) {
            throw new RestaurantException(RestaurantErrorCode.DUPLICATE_RESTAURANT_NAME);
        }

        // 2. Address 생성
        Address address = Address.builder()
                .province(request.getAddress().getProvince())
                .city(request.getAddress().getCity())
                .district(request.getAddress().getDistrict())
                .detailAddress(request.getAddress().getDetailAddress())
                .build();

        if (!address.isValid()) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_ADDRESS);
        }

        // 3. Coordinate 생성 (선택 사항)
        Coordinate coordinate = null;
        if (request.getCoordinate() != null) {
            coordinate = Coordinate.of(
                    request.getCoordinate().getLatitude(),
                    request.getCoordinate().getLongitude()
            );
        }

        // 4. Restaurant Aggregate Root 생성
        Restaurant restaurant = Restaurant.builder()
                .id(generateRestaurantId())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .restaurantName(request.getRestaurantName())
                .contactNumber(request.getContactNumber())
                .address(address)
                .coordinate(coordinate)
                .status(RestaurantStatus.OPEN)
                .isActive(true)
                .createdBy("OWNER_" + ownerId)
                .build();

        // 5. 태그 추가
        if (request.getTags() != null) {
            request.getTags().forEach(restaurant::addTag);
        }

        // 6. 카테고리 연결
        if (request.getCategoryIds() != null) {
            request.getCategoryIds().forEach(categoryId ->
                    restaurant.addRestaurantCategory(categoryId, false, "OWNER_" + ownerId)
            );
        }

        // 7. 도메인 검증
        restaurant.validate();

        // 8. 저장 (영속성 전이로 하위 엔티티 자동 저장)
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("식당 등록 완료 - restaurantId: {}", savedRestaurant.getId());

        // 9. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(savedRestaurant);
        return restaurantMapper.toRestaurantResponse(savedRestaurant, categoryMap);
    }

    /**
     * 식당 정보 전체 수정 (PUT)
     * - Owner 전용
     * - 모든 필드 업데이트
     */
    public RestaurantResponse updateRestaurant(String restaurantId,
                                               RestaurantUpdateRequest request,
                                               String updatedBy) {
        log.info("식당 수정 시작 - restaurantId: {}", restaurantId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. 기본 정보 업데이트
        if (request.getRestaurantName() != null || request.getContactNumber() != null) {
            restaurant.updateBasicInfo(
                    request.getRestaurantName() != null ? request.getRestaurantName() : restaurant.getRestaurantName(),
                    request.getContactNumber() != null ? request.getContactNumber() : restaurant.getContactNumber(),
                    updatedBy
            );
        }

        // 3. 주소 업데이트
        if (request.getAddress() != null) {
            Address address = Address.builder()
                    .province(request.getAddress().getProvince())
                    .city(request.getAddress().getCity())
                    .district(request.getAddress().getDistrict())
                    .detailAddress(request.getAddress().getDetailAddress())
                    .build();
            restaurant.updateAddress(address, updatedBy);
        }

        // 4. 좌표 업데이트
        if (request.getCoordinate() != null) {
            Coordinate coordinate = Coordinate.of(
                    request.getCoordinate().getLatitude(),
                    request.getCoordinate().getLongitude()
            );
            restaurant.updateCoordinate(coordinate, updatedBy);
        }

        // 5. 상태 업데이트
        if (request.getStatus() != null) {
            RestaurantStatus status = RestaurantStatus.valueOf(request.getStatus());
            restaurant.changeStatus(status, updatedBy);
        }

        // 6. 태그 업데이트 (기존 태그 제거 후 새로 추가)
        if (request.getTags() != null) {
            restaurant.getTags().clear();
            request.getTags().forEach(restaurant::addTag);
        }

        // 7. 카테고리 업데이트 (변경사항만 반영)
        if (request.getCategoryIds() != null) {
            updateRestaurantCategories(restaurant, request.getCategoryIds(), updatedBy);
        }

        // 8. 저장 (더티체킹으로 자동 업데이트)
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("식당 수정 완료 - restaurantId: {}", restaurantId);

        // 9. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(savedRestaurant);
        return restaurantMapper.toRestaurantResponse(savedRestaurant, categoryMap);
    }

    /**
     * 식당 정보 부분 수정 (PATCH)
     * - Owner 전용
     * - null이 아닌 필드만 업데이트
     */
    public RestaurantResponse patchRestaurant(String restaurantId,
                                              RestaurantUpdateRequest request,
                                              String updatedBy) {
        log.info("식당 부분 수정 시작 - restaurantId: {}", restaurantId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. 기본 정보 업데이트 (null이 아닌 경우만)
        if (request.getRestaurantName() != null || request.getContactNumber() != null) {
            restaurant.updateBasicInfo(
                    request.getRestaurantName() != null ? request.getRestaurantName() : restaurant.getRestaurantName(),
                    request.getContactNumber() != null ? request.getContactNumber() : restaurant.getContactNumber(),
                    updatedBy
            );
        }

        // 3. 주소 업데이트 (null이 아닌 경우만)
        if (request.getAddress() != null) {
            Address currentAddress = restaurant.getAddress();
            Address address = Address.builder()
                    .province(request.getAddress().getProvince() != null ?
                            request.getAddress().getProvince() : currentAddress.getProvince())
                    .city(request.getAddress().getCity() != null ?
                            request.getAddress().getCity() : currentAddress.getCity())
                    .district(request.getAddress().getDistrict() != null ?
                            request.getAddress().getDistrict() : currentAddress.getDistrict())
                    .detailAddress(request.getAddress().getDetailAddress() != null ?
                            request.getAddress().getDetailAddress() : currentAddress.getDetailAddress())
                    .build();
            restaurant.updateAddress(address, updatedBy);
        }

        // 4. 좌표 업데이트 (null이 아닌 경우만)
        if (request.getCoordinate() != null) {
            Coordinate coordinate = Coordinate.of(
                    request.getCoordinate().getLatitude(),
                    request.getCoordinate().getLongitude()
            );
            restaurant.updateCoordinate(coordinate, updatedBy);
        }

        // 5. 상태 업데이트 (null이 아닌 경우만)
        if (request.getStatus() != null) {
            RestaurantStatus status = RestaurantStatus.valueOf(request.getStatus());
            restaurant.changeStatus(status, updatedBy);
        }

        // 6. 태그 업데이트 (null이 아닌 경우만)
        if (request.getTags() != null) {
            restaurant.getTags().clear();
            request.getTags().forEach(restaurant::addTag);
        }

        // 7. 카테고리 업데이트 (변경사항만 반영)
        if (request.getCategoryIds() != null) {
            updateRestaurantCategories(restaurant, request.getCategoryIds(), updatedBy);
        }

        log.info("식당 부분 수정 완료 - restaurantId: {}", restaurantId);

        // 8. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(restaurant);
        return restaurantMapper.toRestaurantResponse(restaurant, categoryMap);
    }

    /**
     * 식당 삭제 (Soft Delete)
     * - Owner/Admin 공통
     * - 하위 Menu, MenuCategory도 cascade soft delete
     */
    public void deleteRestaurant(String restaurantId, String deletedBy) {
        log.info("식당 삭제 시작 - restaurantId: {}", restaurantId);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. Soft Delete (도메인 메서드 사용)
        restaurant.delete(deletedBy);

        // 3. 저장 (더티체킹으로 자동 업데이트)
        restaurantRepository.save(restaurant);

        log.info("식당 삭제 완료 - restaurantId: {}", restaurantId);
    }

    /**
     * 식당 복구 (Admin 전용)
     * - 삭제된 식당을 복구
     */
    public AdminRestaurantResponse restoreRestaurant(String restaurantId, String updatedBy) {
        log.info("식당 복구 시작 - restaurantId: {}", restaurantId);

        // 1. Restaurant 조회 (삭제된 것 포함)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. 복구
        restaurant.restore(updatedBy);

        // 3. 저장
        Restaurant restoredRestaurant = restaurantRepository.save(restaurant);

        log.info("식당 복구 완료 - restaurantId: {}", restaurantId);

        // 4. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(restoredRestaurant);
        return restaurantMapper.toAdminRestaurantResponse(restoredRestaurant, categoryMap);
    }

    /**
     * 식당 상태 변경 (Admin 전용)
     */
    public AdminRestaurantResponse updateRestaurantStatus(String restaurantId,
                                                          String status,
                                                          String updatedBy) {
        log.info("식당 상태 변경 시작 - restaurantId: {}, status: {}", restaurantId, status);

        // 1. Restaurant 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. 상태 변경
        RestaurantStatus restaurantStatus = RestaurantStatus.valueOf(status);
        restaurant.changeStatus(restaurantStatus, updatedBy);

        // 3. 저장
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("식당 상태 변경 완료 - restaurantId: {}", restaurantId);

        // 4. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(savedRestaurant);
        return restaurantMapper.toAdminRestaurantResponse(savedRestaurant, categoryMap);
    }

    /**
     * 식당 정보 수정 (Admin 전용)
     */
    public AdminRestaurantResponse updateRestaurantByAdmin(String restaurantId,
                                                           AdminRestaurantUpdateRequest request,
                                                           String updatedBy) {
        log.info("식당 수정 시작 (Admin) - restaurantId: {}", restaurantId);

        // 1. Restaurant 조회 (삭제된 것 포함)
        Restaurant restaurant = restaurantRepository.findByIdIncludingDeleted(restaurantId)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.RESTAURANT_NOT_FOUND));

        // 2. 기본 정보 업데이트
        if (request.getRestaurantName() != null || request.getContactNumber() != null) {
            restaurant.updateBasicInfo(
                    request.getRestaurantName() != null ? request.getRestaurantName() : restaurant.getRestaurantName(),
                    request.getContactNumber() != null ? request.getContactNumber() : restaurant.getContactNumber(),
                    updatedBy
            );
        }

        // 3. 주소 업데이트
        if (request.getAddress() != null) {
            Address address = Address.builder()
                    .province(request.getAddress().getProvince())
                    .city(request.getAddress().getCity())
                    .district(request.getAddress().getDistrict())
                    .detailAddress(request.getAddress().getDetailAddress())
                    .build();
            restaurant.updateAddress(address, updatedBy);
        }

        // 4. 좌표 업데이트
        if (request.getCoordinate() != null) {
            Coordinate coordinate = Coordinate.of(
                    request.getCoordinate().getLatitude(),
                    request.getCoordinate().getLongitude()
            );
            restaurant.updateCoordinate(coordinate, updatedBy);
        }

        // 5. 상태 업데이트
        if (request.getStatus() != null) {
            RestaurantStatus status = RestaurantStatus.valueOf(request.getStatus());
            restaurant.changeStatus(status, updatedBy);
        }

        // 6. 활성화 상태 업데이트
        if (request.getIsActive() != null) {
            restaurant.setActive(request.getIsActive(), updatedBy);
        }

        // 7. 저장
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("식당 수정 완료 (Admin) - restaurantId: {}", restaurantId);

        // 8. 카테고리 정보 조회 후 응답 변환
        Map<String, RestaurantCategory> categoryMap = loadCategoriesByRestaurant(savedRestaurant);
        return restaurantMapper.toAdminRestaurantResponse(savedRestaurant, categoryMap);
    }

    /**
     * 카테고리 업데이트 헬퍼 메서드
     * - 삭제된 relation을 복구하거나 새로 추가
     * - 중복 엔티티 생성 방지
     */
    private void updateRestaurantCategories(Restaurant restaurant, Set<String> newCategoryIds, String updatedBy) {
        // 현재 활성화된 카테고리 ID 수집
        Set<String> activeCategoryIds = restaurant.getCategoryRelations().stream()
                .filter(rel -> !rel.isDeleted())
                .map(RestaurantCategoryRelation::getCategoryId)
                .collect(Collectors.toSet());

        // 제거할 카테고리 (기존에는 있지만 요청에는 없음)
        Set<String> categoriesToRemove = activeCategoryIds.stream()
                .filter(id -> !newCategoryIds.contains(id))
                .collect(Collectors.toSet());

        // 추가할 카테고리 (요청에는 있지만 기존에는 없음)
        Set<String> categoriesToAdd = newCategoryIds.stream()
                .filter(id -> !activeCategoryIds.contains(id))
                .collect(Collectors.toSet());

        // 제거 처리 (soft delete)
        categoriesToRemove.forEach(categoryId -> {
            restaurant.getCategoryRelations().stream()
                    .filter(rel -> rel.getCategoryId().equals(categoryId) && !rel.isDeleted())
                    .findFirst()
                    .ifPresent(rel -> rel.delete(updatedBy));
        });

        // 추가 처리
        categoriesToAdd.forEach(categoryId -> {
            // 이미 존재하지만 삭제된 relation이 있는지 확인
            Optional<RestaurantCategoryRelation> deletedRelation = restaurant.getCategoryRelations().stream()
                    .filter(rel -> rel.getCategoryId().equals(categoryId) && rel.isDeleted())
                    .findFirst();

            if (deletedRelation.isPresent()) {
                // 삭제된 relation을 복구
                deletedRelation.get().restore(updatedBy);
            } else {
                // 새로운 relation 추가
                restaurant.addRestaurantCategory(categoryId, false, updatedBy);
            }
        });
    }

    /**
     * Restaurant ID 생성
     * - 형식: REST-{8자리 UUID}
     */
    private String generateRestaurantId() {
        return "REST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Restaurant의 카테고리 정보 일괄 조회
     * - N+1 문제 방지
     */
    private Map<String, RestaurantCategory> loadCategoriesByRestaurant(Restaurant restaurant) {
        Set<String> categoryIds = restaurant.getCategoryRelations().stream()
                .filter(rel -> !rel.isDeleted())
                .map(RestaurantCategoryRelation::getCategoryId)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        // 일괄 조회로 N+1 문제 방지
        List<RestaurantCategory> categories = restaurantCategoryRepository.findAllByIds(categoryIds);

        return categories.stream()
                .collect(Collectors.toMap(
                        RestaurantCategory::getId,
                        category -> category
                ));
    }
}