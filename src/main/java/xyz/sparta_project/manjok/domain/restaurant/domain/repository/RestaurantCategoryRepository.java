package xyz.sparta_project.manjok.domain.restaurant.domain.repository;

import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * RestaurantCategory 도메인 Repository 인터페이스
 */
public interface RestaurantCategoryRepository {

    /**
     * 레스토랑 카테고리 저장 (생성/수정)
     * - ID가 있으면: 기존 엔티티 조회 후 업데이트 (더티체킹)
     * - ID가 없으면: 새로운 엔티티 생성
     * @param category 저장할 카테고리 도메인
     * @return 저장된 카테고리 도메인
     */
    RestaurantCategory save(RestaurantCategory category);

    /**
     * ID로 카테고리 조회
     * @param id 카테고리 ID
     * @return 카테고리 도메인 (Optional)
     */
    Optional<RestaurantCategory> findById(String id);

    /**
     * 여러 ID로 카테고리 일괄 조회
     * - N+1 문제 방지를 위한 일괄 조회
     * - 빈 컬렉션이 들어오면 빈 리스트 반환
     * @param ids 카테고리 ID 목록
     * @return 카테고리 도메인 목록
     */
    List<RestaurantCategory> findAllByIds(Collection<String> ids);

    /**
     * 카테고리 코드로 조회
     * @param categoryCode 카테고리 코드 (예: KOREAN, CHINESE)
     * @return 카테고리 도메인 (Optional)
     */
    Optional<RestaurantCategory> findByCategoryCode(String categoryCode);

    /**
     * 모든 활성화된 카테고리 조회
     * @return 활성화된 카테고리 목록
     */
    List<RestaurantCategory> findAllActive();

    /**
     * 최상위 카테고리 목록 조회
     * @return 최상위 카테고리 목록 (depth = 1)
     */
    List<RestaurantCategory> findRootCategories();

    /**
     * 하위 카테고리 조회
     * @param parentCategoryId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    List<RestaurantCategory> findByParentCategoryId(String parentCategoryId);

    /**
     * 인기 카테고리 조회
     * @return 인기 카테고리 목록
     */
    List<RestaurantCategory> findPopularCategories();

    /**
     * 카테고리 삭제
     * @param id 카테고리 ID
     */
    void deleteById(String id);

    /**
     * 카테고리 존재 여부 확인
     * @param id 카테고리 ID
     * @return 존재 여부
     */
    boolean existsById(String id);
}