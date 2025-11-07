package xyz.sparta_project.manjok.view.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.user.domain.vo.Role;

@Controller
public class AdminController {

    @GetMapping("/view/admin/login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/view/admin")
    public String adminHome() {
        // 인증 정보가 없으면 로그인 페이지로 리다이렉트
        if (SecurityUtils.getCurrentUserDetails().isEmpty()) {
            return "redirect:/view/admin/login";
        }

        // 권한 체크 - MANAGER 또는 MASTER 권한이 있는지 확인
        return SecurityUtils.getCurrentRole()
                .filter(role -> role == Role.MANAGER || role == Role.MASTER)
                .map(role -> "admin")
                .orElse("redirect:/view/admin/login");
    }
}