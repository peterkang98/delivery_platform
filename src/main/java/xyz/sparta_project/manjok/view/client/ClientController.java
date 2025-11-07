package xyz.sparta_project.manjok.view.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.user.domain.vo.Role;

@Controller
public class ClientController {

    @GetMapping("/view/client/login")
    public String clientLogin() {
        return "client-login";
    }

    @GetMapping("/view/client")
    public String clientHome() {
        // 인증 정보가 없으면 로그인 페이지로 리다이렉트
//        if (SecurityUtils.getCurrentUserDetails().isEmpty()) {
//            return "redirect:/view/client/login";
//        }

        return "client";

        // 권한 체크 - CUSTOMER 권한이 있는지 확인
//        return SecurityUtils.getCurrentRole()
//                .filter(role -> role == Role.CUSTOMER)
//                .map(role -> "ROLE_CUSTOMER")
//                .orElse("redirect:/view/client/login");
    }

    @GetMapping("/view/client/reset")
    public String passwordReset() {
        return "password-reset";
    }
}