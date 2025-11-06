package xyz.sparta_project.manjok.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.sparta_project.manjok.user.domain.entity.VerificationToken;
import xyz.sparta_project.manjok.user.domain.vo.TokenType;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationToken, String> {
	Optional<VerificationToken> findByIdAndTokenType(String id, TokenType tokenType);
}
