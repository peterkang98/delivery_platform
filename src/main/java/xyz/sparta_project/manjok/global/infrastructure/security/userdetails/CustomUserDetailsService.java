package xyz.sparta_project.manjok.global.infrastructure.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.exception.UserErrorCode;
import xyz.sparta_project.manjok.user.exception.UserException;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.INVALID_LOGIN_EMAIL;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다"));
		return new CustomUserDetails(user);
	}
}
