package com.ayderbek.userservice.service;

import com.ayderbek.userservice.domain.User;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String profilePicture;

    public static UserResponse fromUser(User user) {
        ModelMapper modelMapper = new ModelMapper();

        // Define the mapping configuration (if needed)
        // modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserResponse response = modelMapper.map(user, UserResponse.class);
        return response;
    }
}
