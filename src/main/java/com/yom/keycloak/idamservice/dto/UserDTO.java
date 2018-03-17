package com.yom.keycloak.idamservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yogendra on 17/3/18.
 */
@Setter
@Getter
public class UserDTO {

    private String id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
}
