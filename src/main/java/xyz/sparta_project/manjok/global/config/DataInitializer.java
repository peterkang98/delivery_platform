package xyz.sparta_project.manjok.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.restaurant.application.service.RestaurantCommandService;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.*;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantCategoryRepository;
import xyz.sparta_project.manjok.domain.restaurant.domain.repository.RestaurantRepository;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.domain.vo.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final RestaurantRepository restaurantRepository;

    // 서울과 부산의 각 구별 중심 좌표
    private static final Map<String, double[]> SEOUL_DISTRICTS = Map.of(
            "강남구", new double[]{37.4979, 127.0276},
            "서초구", new double[]{37.4837, 127.0324},
            "송파구", new double[]{37.5145, 127.1059},
            "강동구", new double[]{37.5301, 127.1238},
            "종로구", new double[]{37.5735, 126.9788}
    );

    private static final Map<String, double[]> BUSAN_DISTRICTS = Map.of(
            "해운대구", new double[]{35.1631, 129.1633},
            "부산진구", new double[]{35.1628, 129.0533},
            "동래구", new double[]{35.2046, 129.0839},
            "남구", new double[]{35.1360, 129.0845},
            "수영구", new double[]{35.1453, 129.1132}
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== 데이터 초기화 시작 ===");

        // 1. 사용자 생성
        Map<String, User> users = createUsers();

        // 2. 레스토랑 카테고리 생성 (마스터 권한으로)
        List<RestaurantCategory> categories = createRestaurantCategories(users.get("testMaster"));

        // 3. 레스토랑 생성
        createRestaurants(users, categories);

        log.info("=== 데이터 초기화 완료 ===");
    }

    /**
     * 사용자 생성
     */
    private Map<String, User> createUsers() {
        log.info("사용자 생성 시작...");

        Map<String, User> users = new HashMap<>();
        String password = passwordEncoder.encode("Qwer1234!");

        // 클라이언트 1명
        User client = User.builder()
                .username("testClient")
                .email("testClient@test.com")
                .password(password)
                .build();
        client.verify(); // 이메일 인증 완료
        users.put("testClient", userRepository.save(client));

        // 오너 2명
        User owner1 = User.builder()
                .username("testOwner1")
                .email("testOwner1@test.com")
                .password(password)
                .build();
        owner1.verify();
        owner1.promoteRole(Role.OWNER, () -> true);
        users.put("testOwner1", userRepository.save(owner1));

        User owner2 = User.builder()
                .username("testOwner2")
                .email("testOwner2@test.com")
                .password(password)
                .build();
        owner2.verify();
        owner2.promoteRole(Role.OWNER, () -> true);
        users.put("testOwner2", userRepository.save(owner2));

        // 마스터 1명
        User master = User.builder()
                .username("testMaster")
                .email("testMaster@test.com")
                .password(password)
                .build();
        master.verify();
        master.promoteRole(Role.MASTER, () -> true);
        users.put("testMaster", userRepository.save(master));

        log.info("사용자 생성 완료 - 총 {}명", users.size());
        return users;
    }

    /**
     * 레스토랑 카테고리 생성 (대분류만)
     */
    private List<RestaurantCategory> createRestaurantCategories(User master) {
        log.info("레스토랑 카테고리 생성 시작...");

        List<RestaurantCategory> categories = new ArrayList<>();
        String createdBy = "MASTER_" + master.getUsername();

        // 대분류 카테고리 정의
        String[][] categoryData = {
                {"KOREAN", "한식", "한국 전통 음식", "#FF6B6B"},
                {"CHINESE", "중식", "중국 음식", "#FFA500"},
                {"JAPANESE", "일식", "일본 음식", "#FFD93D"},
                {"WESTERN", "양식", "서양 음식", "#6BCF7F"},
                {"CHICKEN", "치킨", "프라이드 치킨 및 치킨 요리", "#FFC312"},
                {"PIZZA", "피자", "피자 전문점", "#C44569"},
                {"BURGER", "버거", "햄버거 전문점", "#A55C3D"},
                {"CAFE", "카페/디저트", "카페 및 디저트", "#778BEB"},
                {"FASTFOOD", "패스트푸드", "패스트푸드 체인", "#F8B500"},
                {"ASIAN", "아시안", "동남아 및 기타 아시아 음식", "#3DC1D3"}
        };

        for (int i = 0; i < categoryData.length; i++) {
            String[] data = categoryData[i];
            RestaurantCategory category = RestaurantCategory.builder()
                    .id("CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .createdAt(LocalDateTime.now())
                    .categoryCode(data[0])
                    .categoryName(data[1])
                    .description(data[2])
                    .colorCode(data[3])
                    .depth(1)
                    .displayOrder(i)
                    .isActive(true)
                    .isPopular(i < 5) // 처음 5개는 인기 카테고리
                    .createdBy(createdBy)
                    .build();

            categories.add(restaurantCategoryRepository.save(category));
        }

        log.info("레스토랑 카테고리 생성 완료 - 총 {}개", categories.size());
        return categories;
    }

    /**
     * 레스토랑 생성
     */
    private void createRestaurants(Map<String, User> users, List<RestaurantCategory> categories) {
        log.info("레스토랑 생성 시작...");

        User owner1 = users.get("testOwner1");
        User owner2 = users.get("testOwner2");

        // Owner1 - 서울 5개, 부산 5개
        createRestaurantsForOwner(owner1, categories, true);

        // Owner2 - 서울 5개, 부산 5개
        createRestaurantsForOwner(owner2, categories, false);

        log.info("레스토랑 생성 완료");
    }

    /**
     * 특정 오너의 레스토랑 생성
     */
    private void createRestaurantsForOwner(User owner, List<RestaurantCategory> categories, boolean isOwner1) {
        String ownerPrefix = isOwner1 ? "오너1" : "오너2";
        Long ownerId = Long.valueOf(owner.getId().hashCode());
        String createdBy = "OWNER_" + owner.getUsername();

        // 서울 5개 레스토랑
        List<String> seoulDistricts = new ArrayList<>(SEOUL_DISTRICTS.keySet());
        for (int i = 0; i < 5; i++) {
            String district = seoulDistricts.get(i);
            double[] coords = SEOUL_DISTRICTS.get(district);
            RestaurantCategory category = categories.get((i + (isOwner1 ? 0 : 5)) % categories.size());

            Restaurant restaurant = createRestaurant(
                    ownerId,
                    owner.getUsername(),
                    String.format("[%s] 서울 %s %s", ownerPrefix, district, category.getCategoryName()),
                    "서울특별시",
                    district,
                    getSubDistrict(district, i),
                    String.format("%d번지 %d층", (i + 1) * 10, i + 1),
                    coords[0],
                    coords[1],
                    category,
                    createdBy
            );

            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            log.info("레스토랑 생성: {}", savedRestaurant.getRestaurantName());

            // 메뉴 카테고리 및 메뉴 생성
            createMenusForRestaurant(savedRestaurant, createdBy);
        }

        // 부산 5개 레스토랑
        List<String> busanDistricts = new ArrayList<>(BUSAN_DISTRICTS.keySet());
        for (int i = 0; i < 5; i++) {
            String district = busanDistricts.get(i);
            double[] coords = BUSAN_DISTRICTS.get(district);
            RestaurantCategory category = categories.get((i + 5 + (isOwner1 ? 0 : 5)) % categories.size());

            Restaurant restaurant = createRestaurant(
                    ownerId,
                    owner.getUsername(),
                    String.format("[%s] 부산 %s %s", ownerPrefix, district, category.getCategoryName()),
                    "부산광역시",
                    district,
                    getSubDistrict(district, i),
                    String.format("%d번지 %d층", (i + 1) * 10, i + 1),
                    coords[0],
                    coords[1],
                    category,
                    createdBy
            );

            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            log.info("레스토랑 생성: {}", savedRestaurant.getRestaurantName());

            // 메뉴 카테고리 및 메뉴 생성
            createMenusForRestaurant(savedRestaurant, createdBy);
        }
    }

    /**
     * 레스토랑 객체 생성
     */
    private Restaurant createRestaurant(Long ownerId, String ownerName, String name,
                                        String province, String city, String district,
                                        String detailAddress, double lat, double lon,
                                        RestaurantCategory category, String createdBy) {
        Address address = Address.builder()
                .province(province)
                .city(city)
                .district(district)
                .detailAddress(detailAddress)
                .build();

        Coordinate coordinate = Coordinate.of(lat, lon);

        Restaurant restaurant = Restaurant.builder()
                .id("REST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .createdAt(LocalDateTime.now())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .restaurantName(name)
                .contactNumber("02-" + String.format("%04d", new Random().nextInt(10000)) + "-" + String.format("%04d", new Random().nextInt(10000)))
                .address(address)
                .coordinate(coordinate)
                .status(RestaurantStatus.CLOSED)  // 메뉴 생성을 위해 CLOSED 상태로 시작
                .isActive(true)
                .createdBy(createdBy)
                .build();

        // 레스토랑 카테고리 연결
        restaurant.addRestaurantCategory(category.getId(), true, createdBy);

        // 운영시간 설정 (평일)
        for (DayType dayType : DayType.values()) {
            if (dayType.isWeekday()) {
                restaurant.setOperatingDay(
                        dayType,
                        OperatingTimeType.REGULAR,
                        LocalTime.of(10, 0),
                        LocalTime.of(22, 0),
                        false,
                        null
                );
            } else {
                restaurant.setOperatingDay(
                        dayType,
                        OperatingTimeType.REGULAR,
                        LocalTime.of(11, 0),
                        LocalTime.of(23, 0),
                        false,
                        null
                );
            }
        }

        return restaurant;
    }

    /**
     * 레스토랑의 메뉴 카테고리 및 메뉴 생성
     */
    private void createMenusForRestaurant(Restaurant restaurant, String createdBy) {
        // 메뉴 카테고리 2개 생성
        MenuCategory mainCategory = restaurant.addMenuCategory(
                "메인 메뉴",
                "대표 메인 메뉴",
                null,
                0,
                createdBy
        );

        MenuCategory sideCategory = restaurant.addMenuCategory(
                "사이드 메뉴",
                "사이드 메뉴 및 음료",
                null,
                1,
                createdBy
        );

        // 각 카테고리별로 메뉴 10개 생성
        createMenusForCategory(restaurant, mainCategory, 10, createdBy, true);
        createMenusForCategory(restaurant, sideCategory, 10, createdBy, false);

        // 모든 메뉴 생성 완료 후 레스토랑 상태를 OPEN으로 변경
        restaurant.changeStatus(RestaurantStatus.OPEN, createdBy);

        restaurantRepository.save(restaurant);
    }

    /**
     * 특정 카테고리에 메뉴 생성
     */
    private void createMenusForCategory(Restaurant restaurant, MenuCategory category,
                                        int count, String createdBy, boolean isMain) {
        for (int i = 0; i < count; i++) {
            String menuName = String.format("%s %d번 메뉴", category.getCategoryName(), i + 1);
            // 모든 메뉴 가격 10원으로 통일
            BigDecimal price = BigDecimal.valueOf(10);

            Menu menu = restaurant.addMenu(
                    menuName,
                    String.format("%s 입니다", menuName),
                    price,
                    createdBy
            );

            // 카테고리 연결
            restaurant.addMenuToCategory(menu.getId(), category.getId(), true, createdBy);

            // 메뉴 상태 설정
            if (i < 3) {
                menu.setMain(true, createdBy);
            }
            if (i < 2) {
                menu.setPopular(true, createdBy);
            }

            // 옵션 그룹 2개 추가
            createOptionGroupsForMenu(restaurant, menu, createdBy);
        }
    }

    /**
     * 메뉴의 옵션 그룹 생성
     */
    private void createOptionGroupsForMenu(Restaurant restaurant, Menu menu, String createdBy) {
        // 옵션 그룹 1: 사이즈 선택
        MenuOptionGroup sizeGroup = restaurant.addOptionGroupToMenu(
                menu.getId(),
                "사이즈 선택",
                "메뉴 사이즈를 선택하세요",
                true,
                1,
                1,
                createdBy
        );

        // 사이즈 옵션 2개 (모든 옵션 가격 1원으로 통일)
        restaurant.addOptionToGroup(menu.getId(), sizeGroup.getId(), "일반", 1, 0, createdBy);
        restaurant.addOptionToGroup(menu.getId(), sizeGroup.getId(), "대", 1, 1, createdBy);

        // 옵션 그룹 2: 추가 옵션
        MenuOptionGroup extraGroup = restaurant.addOptionGroupToMenu(
                menu.getId(),
                "추가 옵션",
                "추가 옵션을 선택하세요",
                false,
                0,
                3,
                createdBy
        );

        // 추가 옵션 2개 (모든 옵션 가격 1원으로 통일)
        restaurant.addOptionToGroup(menu.getId(), extraGroup.getId(), "치즈 추가", 1, 0, createdBy);
        restaurant.addOptionToGroup(menu.getId(), extraGroup.getId(), "야채 추가", 1, 1, createdBy);
    }

    /**
     * 구별 하위 동 이름 생성
     */
    private String getSubDistrict(String district, int index) {
        String[] suffixes = {"동", "역삼동", "신사동", "논현동", "대치동"};
        return district.replace("구", "") + suffixes[index % suffixes.length];
    }
}