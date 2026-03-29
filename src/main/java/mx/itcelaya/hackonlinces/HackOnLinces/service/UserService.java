package mx.itcelaya.hackonlinces.HackOnLinces.service;

import lombok.RequiredArgsConstructor;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.UserProfileResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.exception.ResourceNotFoundException;
import mx.itcelaya.hackonlinces.HackOnLinces.mapper.UserMapper;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Recargar con roles para evitar LazyInitializationException en el mapper
        user = userRepository.findByEmailWithRoles(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return userMapper.toProfileResponse(user);
    }
}