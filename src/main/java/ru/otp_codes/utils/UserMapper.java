package ru.otp_codes.utils;

import ru.otp_codes.dto.UserRegDto;
import ru.otp_codes.model.User;

public class UserMapper {
    public static User fromDto(UserRegDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(PasswordEncoder.hash(dto.getPassword()));
        String role = dto.isAdmin() ? "admin" : "user";
        user.setRole(role);
        return user;
    }

}
