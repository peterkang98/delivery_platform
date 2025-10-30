package xyz.sparta_project.manjok.global.common.utils;

import org.springframework.data.domain.Page;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageUtils {
    
    /**
     * Spring Data Page를 PageResponse로 변경
     * */
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        validatePage(page);

        PageInfo pageInfo = createPageInfo(page);
        return PageResponse.of(page.getContent(), pageInfo);
    }

    /**
     * Spring Data Page를 매퍼 함수를 사용하여 PageResponse로 변환
     * 엔티티 -> DTO 변환 시 사용
     * */
    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        validatePage(page);
        validateMapper(mapper);

        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        PageInfo pageInfo = createPageInfo(page);
        return PageResponse.of(mappedContent, pageInfo);
    }


    private static <T> PageInfo createPageInfo(Page<T> page) {
        return PageInfo.of(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements()
        );
    }

    private static void validatePage(Page<?> page) {
        if (page == null) {
            throw new IllegalArgumentException("페이지에는 null이 들어갈 수 없습니다.");
        }
    }

    private static void validateMapper(Function<?,?> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("매퍼는 null이 들어갈 수 없습니다.");
        }
    }
}
