package com.example.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordChangeVO {
    @Length(min = 6, max = 20)
    String password;
    @Length(min = 6, max = 20)
    String newPassword;
}
