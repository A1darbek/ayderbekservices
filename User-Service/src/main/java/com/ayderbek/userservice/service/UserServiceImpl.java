package com.ayderbek.userservice.service;

import com.amazonaws.services.alexaforbusiness.model.NotFoundException;
import com.ayderbek.userservice.S3.S3Config;
import com.ayderbek.userservice.S3.S3Service;
import com.ayderbek.userservice.domain.User;
import com.ayderbek.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3Config s3Buckets;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void uploadProfileImage(String userId, MultipartFile file) {
        String profilePictureId = UUID.randomUUID().toString();
        try {
            byte[] profilePictureBytes = file.getBytes();
            String s3Key = "Profile-images/%s/%s".formatted(userId, profilePictureId);
            s3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    profilePictureBytes
            );

            String cloudFrontUrl = "https://%s/%s".formatted(cloudFrontDomain, s3Key);

            User user = getById(userId);
            user.setProfilePicture(cloudFrontUrl);
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("failed to upload profile image", e);
        }
    }

    @Override
    public User getById(String usedId) {
        return userRepository.findById(usedId)
                .orElseThrow(() -> new NotFoundException("user not found with id " + usedId));
    }

    @Override
    public String getProfileImageUrl(String userId) {
        User user = getById(userId);
        return user.getProfilePicture();
    }

    @Override
    public UserResponse getUserWithDetails(String userId) {
        User user = getById(userId);

        String profileImageBytes = getProfileImageUrl(userId);

        UserResponse userResponse = UserResponse.fromUser(user);
        userResponse.setProfilePicture(profileImageBytes);

        return userResponse;
    }
}
