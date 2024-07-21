package com.example.demo.user.dto;


import lombok.Data;


@Data

public class SignUpRequestDto {

    private String nickName;
    private String email;
    private String password;
    private String tags;

}
