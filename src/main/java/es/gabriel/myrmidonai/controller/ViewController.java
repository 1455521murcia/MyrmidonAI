package es.gabriel.myrmidonai.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewController {


    @GetMapping("/chat")
    @PreAuthorize("isAuthenticated()") // Aqui se usa para spring security que garantiza que solo los usuarios que han pasado por el login puedan acceder
    public String chatPage(){
        return "chat";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

}
