package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.model.enums.UserRole;
import app.accommodationbookingservice.repository.UserRepository;
import app.accommodationbookingservice.security.JwtTokenProvider;
import app.accommodationbookingservice.service.UserService;
import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
    public User register(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        u.setRole(UserRole.CUSTOMER);
        return repo.save(u);
    }

    @Override
    public User updateRole(Long id, String role) {
        User u = repo.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + id)
        );
        u.setRole(UserRole.valueOf(role));
        return repo.save(u);
    }

    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Override
    public User save(User u) {
        return repo.save(u);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll();
    }
}
