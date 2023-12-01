package com.ayderbek.userservice.service;

import com.ayderbek.userservice.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    void uploadProfileImage(String userId, MultipartFile file);

   User getById(String usedId);

   String getProfileImageUrl(String userId);

   UserResponse getUserWithDetails(String userId);
}
