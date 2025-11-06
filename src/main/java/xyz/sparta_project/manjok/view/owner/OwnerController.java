package xyz.sparta_project.manjok.view.owner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwnerController {

    @GetMapping("/view/owner")
    public String ownerHome() {
        return "owner";
    }
}
