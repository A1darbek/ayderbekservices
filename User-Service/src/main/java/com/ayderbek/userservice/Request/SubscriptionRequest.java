package com.ayderbek.userservice.Request;

import lombok.Data;

import java.util.Date;

@Data
public class SubscriptionRequest {
    private Long userId;
    private String plan;
    private Date startDate;
    private Date endDate;
}
