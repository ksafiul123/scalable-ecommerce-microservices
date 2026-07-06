package user_service.service;

import user_service.dto.UserDTO;
import user_service.entity.User;
import user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Called after registration in auth-service to create profile
    @Transactional
    public UserDTO createProfile(UserDTO dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("Profile already exists for userId: " + dto.getUserId());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUserId(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        userRepository.save(user);
        return toDTO(user);
    }

    public UserDTO getProfileByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        return toDTO(user);
    }

    public UserDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found for email: " + email));
        return toDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UserDTO dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone()    != null) user.setPhone(dto.getPhone());
        if (dto.getAddress()  != null) user.setAddress(dto.getAddress());

        userRepository.save(user);
        return toDTO(user);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        userRepository.delete(user);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
