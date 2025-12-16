package com.grabbill.server.dto;

import com.grabbill.core.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author michaellow
 **/
@AllArgsConstructor
@Data
public class GrabbillToken {

    private Type type;

    private UserType userType;

    private String jwtTokenValue;

    private Long duration;

    private LocalDateTime expiryDate;


    public enum Type {

        ACCESS,
        REFRESH

    }

}
