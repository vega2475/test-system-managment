package com.testmanagementsystem.dto.email;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailChangeRequest {

    @NotBlank
    @Email
    private String newEmail;
}
