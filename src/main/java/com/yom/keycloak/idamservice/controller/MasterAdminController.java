package com.yom.keycloak.idamservice.controller;

import com.sun.media.sound.InvalidDataException;
import com.yom.keycloak.idamservice.dto.CreateUserRequest;
import com.yom.keycloak.idamservice.dto.UserDTO;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

import static com.yom.keycloak.idamservice.constants.IdamConstant.ACTION_UPDATE_PASSWORD;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Created by yogendra
 */
@RestController
public class MasterAdminController {

    @Value("${keycloak.url}")
    String serverUrl;

    @Value("${keycloak.realm}")
    String masterRealm;

    @Value("${keycloak.username}")
    String masterUsername;

    @Value("${keycloak.password}")
    String masterPassword;

    @Value("${keycloak.clientId}")
    String masterClientId;


    @PostMapping(path = "/user", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public UserDTO createUser(@RequestBody CreateUserRequest request) throws InvalidDataException {

        Keycloak kcMaster = Keycloak.getInstance(serverUrl, masterRealm, masterUsername, masterPassword, masterClientId);

        CredentialRepresentation credential = getCredentialRepresentation(request.getPassword(), true);
        UserRepresentation user = getNewUserRepresentation(request, credential);
        kcMaster.realm(request.getCompanyName()).users().create(user);

        UserDTO newUser = buildResponse(request, kcMaster);
        return newUser;
    }


    @PostMapping(path = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public AccessTokenResponse getToken(@RequestBody Map<String, Object> credentials) {
        Keycloak kc = Keycloak.getInstance(
                serverUrl,
                (String) credentials.get("company"),
                (String) credentials.get("username"), (String) credentials.get("password"),
                (String) credentials.get("clientId"));

        return kc.tokenManager().getAccessToken();

    }


    private UserDTO buildResponse(CreateUserRequest request, Keycloak kcMaster) throws InvalidDataException {

        return kcMaster.realms().realm(request.getCompanyName()).users().search(request
                .getUserName())
                .stream()
                .filter(u -> u.getUsername().equals(request.getUserName()))
                .findFirst().map(this::convertToUserDTO).orElseThrow(InvalidDataException::new);
    }

    private UserDTO convertToUserDTO(UserRepresentation userRepresentation) {

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userRepresentation.getId());
        userDTO.setUserName(userRepresentation.getUsername());
        userDTO.setFirstName(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());
        userDTO.setEmail(userRepresentation.getEmail());
        return userDTO;
    }

    private CredentialRepresentation getCredentialRepresentation(String password, boolean isTempPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(isTempPassword);
        return credential;
    }

    private UserRepresentation getNewUserRepresentation(CreateUserRequest userCreationParam, CredentialRepresentation credential) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userCreationParam.getUserName());
        user.setFirstName(userCreationParam.getFirstName());
        user.setLastName(userCreationParam.getLastName());
        user.setEmail(userCreationParam.getEmail());
        user.setCredentials(Arrays.asList(credential));
        user.setRequiredActions(Arrays.asList(ACTION_UPDATE_PASSWORD));
        user.setEnabled(true);
        return user;
    }

}
