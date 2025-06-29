package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.model.enums.UserRole;
import app.accommodationbookingservice.repository.UserRepository;
import app.accommodationbookingservice.security.JwtTokenProvider;
import app.accommodationbookingservice.service.UserService;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    public UserServiceImpl(
            UserRepository repo,
            PasswordEncoder encoder,
            JwtTokenProvider jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Override
    @Transactional
    public User register(User u) {
        if (repo.existsByEmail(u.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email already in use"
            );
        }

        // Validation of password should be handled in DTO using annotations like:
        // @Size(min = 8), @Pattern(...)
        u.setPassword(encoder.encode(u.getPassword()));
        u.setRole(UserRole.CUSTOMER);
        return repo.save(u);
    }

    @Override
    @Transactional
    public User updateRole(Long id, String role) {
        User u = repo.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + id)
        );

        try {
            u.setRole(UserRole.valueOf(role));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: '" + role + "'. Valid roles: "
                            + Arrays.toString(UserRole.values())
            );
        }

        return repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    @Transactional
    public User save(User u) {
        return repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repo.findAll();
    }
}
