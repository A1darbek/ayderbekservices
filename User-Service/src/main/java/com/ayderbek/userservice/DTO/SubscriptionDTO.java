package com.ayderbek.userservice.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class SubscriptionDTO {
    private Long id;
    private Long userId;
    private String plan;
    private Date startDate;
    private Date endDate;
}
