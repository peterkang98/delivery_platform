package xyz.sparta_project.manjok.global.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.common.utils.UuidUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseEntity")
class BaseEntityTest {

    @Test
    @DisplayName("ID를 자동 생성한다.")
    void onCreate_generates_id() {
        //Given & When
        BaseEntity entity = new BaseEntity();

        //Then
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    @DisplayName("생성된 ID는 36자다.")
    void generated_id_is_36_chars() {
        //Given & When
        BaseEntity entity = new BaseEntity();

        //Then
        assertThat(entity.getId()).hasSize(36);
    }

    @Test
    @DisplayName("생성된 ID는 유효한 UUID이다.")
    void generated_id_is_valid_uuid() {
        //Given & When
        BaseEntity entity = new BaseEntity();

        //Then
        assertThat(UuidUtils.isValid(entity.getId())).isTrue();
    }

    @Test
    @DisplayName("createdAt을 자동 설정한다.")
    void onCreate_sets_created_at() {
        //Given & When
        BaseEntity entity = new BaseEntity();

        //Then
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createdAt은 현재 서버 시간이다.")
    void created_at_is_current_server_time() {
        //Given
        LocalDateTime before = LocalDateTime.now();
        BaseEntity entity = new BaseEntity();

        //Then
        LocalDateTime after = LocalDateTime.now();
        assertThat(entity.getCreatedAt())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);

    }

    @Test
    @DisplayName("매번 다른 ID를 생성한다.")
    void generates_unique_ids() {
        //Given & When
        BaseEntity entity1 = new BaseEntity();
        BaseEntity entity2 = new BaseEntity();

        //Then
        assertThat(entity1.getId()).isNotEqualTo(entity2.getId());
    }

    @Test
    @DisplayName("이미 ID와 createdAt가 있으면 새로 생성하지 않는다.")
    void does_not_override_existing_id() {
        //Given & When
        BaseEntity entity = new BaseEntity();
        String existingId = entity.getId();
        LocalDateTime existingCreatedAt = entity.getCreatedAt();

        //Then
        assertThat(entity.getId()).isEqualTo(existingId);
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
    }

    @Test
    @DisplayName("같은 ID를 가진 엔티티는 같다.")
    void entities_with_same_id_are_equal() throws Exception {
        //Given & When
        BaseEntity entity1 = new BaseEntity();
        BaseEntity entity2 = new BaseEntity();

        var field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        String sameId = field.get(entity1).toString();
        field.set(entity2, sameId);

        //When & Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("다른 ID를 가진 엔티티는 다르다.")
    void entities_with_different_ids_are_not_equal() {
        //Given & When
        BaseEntity entity1 = new BaseEntity();
        BaseEntity entity2 = new BaseEntity();

        //When
        assertThat(entity1).isNotEqualTo(entity2);

    }


}