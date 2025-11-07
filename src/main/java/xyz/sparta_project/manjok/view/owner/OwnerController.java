package xyz.sparta_project.manjok.view.owner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.user.domain.vo.Role;

@Controller
public class OwnerController {

    @GetMapping("/view/owner/login")
    public String ownerLogin() {
        return "owner-login";
    }

    @GetMapping("/view/owner")
    public String ownerHome() {
        // 인증 정보가 없으면 로그인 페이지로 리다이렉트
        if (SecurityUtils.getCurrentUserDetails().isEmpty()) {
            return "redirect:/view/owner/login";
        }

        // 권한 체크 - OWNER 권한이 있는지 확인
        return SecurityUtils.getCurrentRole()
                .filter(role -> role == Role.OWNER)
                .map(role -> "owner")
                .orElse("redirect:/view/owner/login");
    }
}