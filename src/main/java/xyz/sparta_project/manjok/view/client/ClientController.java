package xyz.sparta_project.manjok.view.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    @GetMapping("/view/client")
    public String clientHome() {
        return "client";
    }
}
