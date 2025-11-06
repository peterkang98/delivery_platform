package xyz.sparta_project.manjok.view.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/view/admin")
    public String adminHome() {
        return "admin";
    }
}
