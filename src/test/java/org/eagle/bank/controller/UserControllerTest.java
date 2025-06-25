package org.eagle.bank.controller;

import org.eagle.bank.JsonReader;
import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class UserControllerTest {


    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;

    static JsonReader jsonReader = new JsonReader();

    void setUp() {
    }

    @Test
    void test_success() {
        CreateUserRequest userRequest = jsonReader.jsonToObject("/create_user_request.json", CreateUserRequest.class);
        Assertions.assertNotNull(userRequest);
    }



}
