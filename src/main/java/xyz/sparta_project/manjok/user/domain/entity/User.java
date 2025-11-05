package xyz.sparta_project.manjok.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.user.domain.vo.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {
	@Column(length = 100, nullable = false, unique = true)
	private String email;

	@Column(length = 10, nullable = false, unique = true)
	private String username;

	@Column(length = 60, nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role = Role.CUSTOMER;

	@OneToMany
	private List<Address> addresses = new ArrayList<>();

	@Column(name = "is_verified", nullable = false)
	private Boolean isVerified = false;

	@Column(name = "is_public", nullable = false)
	private Boolean isPublic = false;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", length = 10)
	private String updatedBy;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 10)
	private String deletedBy;

	@Column(name = "created_by", length = 10)
	private String createdBy;

	@Builder
	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public void verify() {
		this.isVerified = true;
	}

	public void updatePassword(String newPassword) {
		this.password = newPassword;
	}

	public void updateRole(Role newRole, String updatedBy) {
		this.role = newRole;
		this.updatedBy = updatedBy;
	}

	public void softDelete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}
