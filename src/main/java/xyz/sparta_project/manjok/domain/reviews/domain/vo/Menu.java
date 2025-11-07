package xyz.sparta_project.manjok.domain.reviews.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Menu {
	@Column(nullable = false)
	private String menuId;
	@Column(nullable = false)
	private String menuName;

	public Menu(String menuId, String menuName) {
		this.menuId = menuId;
		this.menuName = menuName;
	}
}
