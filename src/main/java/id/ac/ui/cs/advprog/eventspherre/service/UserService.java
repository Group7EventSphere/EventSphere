package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(String roleName) {
        try {
            User.Role role = User.Role.valueOf(roleName.toUpperCase());
            return userRepository.findByRole(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
    
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findByNameOrEmailContainingIgnoreCase(searchTerm.trim());
    }
    
    public List<User> searchUsersByRoleAndTerm(String roleName, String searchTerm) {
        try {
            User.Role role = User.Role.valueOf(roleName.toUpperCase());
            
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return userRepository.findByRole(role);
            }
            
            return userRepository.findByRoleAndNameOrEmailContainingIgnoreCase(role, searchTerm.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
    
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    public User updateUserRole(Integer userId, String roleName) {
        User user = getUserById(userId);
        try {
            User.Role role = User.Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
    
    public User updateUser(Integer userId, String name, String email, String phoneNumber) {
        User user = getUserById(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }
    
    public User updateUserPassword(Integer userId, String newPassword) {
        User user = getUserById(userId);
        // Encode the password before storing
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }
}
