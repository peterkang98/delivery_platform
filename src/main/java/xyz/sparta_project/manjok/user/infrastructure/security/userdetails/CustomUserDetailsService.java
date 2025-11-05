package xyz.sparta_project.manjok.user.infrastructure.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다"));
		return new CustomUserDetails(user);
	}
}
