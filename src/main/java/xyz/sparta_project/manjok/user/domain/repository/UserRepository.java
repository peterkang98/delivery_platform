package xyz.sparta_project.manjok.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import xyz.sparta_project.manjok.user.domain.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
	boolean existsByEmail(String email);
	boolean existsByUsername(String username);
	Optional<User> findByEmail(String email);
}
