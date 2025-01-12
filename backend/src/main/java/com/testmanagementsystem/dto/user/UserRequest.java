package com.testmanagementsystem.dto.user;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String surname;
    private String email;
}
