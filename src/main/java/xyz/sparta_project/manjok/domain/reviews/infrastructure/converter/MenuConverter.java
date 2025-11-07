package xyz.sparta_project.manjok.domain.reviews.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import org.springframework.util.StringUtils;
import xyz.sparta_project.manjok.domain.reviews.domain.vo.Menu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MenuConverter implements AttributeConverter<List<Menu>, String> {
	@Override
	public String convertToDatabaseColumn(List<Menu> attribute) {
		return attribute == null ? null : attribute.stream()
				.map(menu -> String.format("%s__%s", menu.getMenuId(), menu.getMenuName()))
				.collect(Collectors.joining("||"));

	}

	@Override
	public List<Menu> convertToEntityAttribute(String dbData) {
		return StringUtils.hasText(dbData) ? null : Arrays.stream(dbData.split("||"))
				.map(s -> {
					String[] values = s.split("__");
					return new Menu(values[0], values[1]);
				}).toList();

	}
}
