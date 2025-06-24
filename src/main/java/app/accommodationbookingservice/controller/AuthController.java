package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.security.JwtTokenProvider;
import app.accommodationbookingservice.service.UserService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;
    private final UserService userService;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider jwt,
                          UserService userService,
                          PasswordEncoder encoder) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.userService = userService;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User u) {

        return ResponseEntity.ok(userService.register(u));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.get("email"), body.get("password"))
        );
        String token = jwt.createToken(auth.getName(),
                auth.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
