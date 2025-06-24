package app.accommodationbookingservice.service;

import app.accommodationbookingservice.model.User;
import java.util.List;

public interface UserService {
    User register(User u);

    User updateRole(Long id, String role);

    User findByEmail(String email);

    User save(User u);

    List<User> findAll();
}
