package org.eagle.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.dto.CreateUserRequestAddress;
import org.eagle.bank.model.Address;
import org.eagle.bank.model.User;
import org.eagle.bank.repository.UserRepository;
import org.eagle.bank.security.JwtAuthenticationFilter;
import org.eagle.bank.security.JwtUtil;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc (addFilters = false)// disables security filters for this test
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
     private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    ObjectMapper objectMapper = new ObjectMapper();

    User user;

    @BeforeEach
    public void setup(){

        user = new User();
        Address address = new Address();
        address.setLine1("123 Main St");
        address.setTown("London");
        address.setCounty("London");
        address.setPostcode("ABC 123");
        user.setAddress(address);
        user.setName("John Doe");
        user.setPassword("Test1234!");
        user.setEmail("john@example.com");
        user.setPhoneNumber("1234567890");
        user.setUsername("john1");
    }

    @Test
    void create_user_success() throws Exception {

        CreateUserRequest req = new CreateUserRequest();
        CreateUserRequestAddress address = new CreateUserRequestAddress();
        address.setLine1("123 Main St");
        address.setTown("London");
        address.setCounty("London");
        address.setPostcode("ABC 123");
        req.setAddress(address);
        req.name("John Doe");
        req.setPassword("Test1234!");
        req.setEmail("john@example.com");
        req.setPhoneNumber("1234567890");
        req.setUsername("john1");

        String requestJson = objectMapper.writeValueAsString(req);
        given(userService.createUser(any())).willReturn(user);

        ResultActions resultActions = mockMvc.perform(
                post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );
        // Assert the status
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.username", "john1").exists());
    }

    @Test
    void missing_req_fields_user_create() throws Exception {

        CreateUserRequest req = new CreateUserRequest();
        CreateUserRequestAddress address = new CreateUserRequestAddress();

        address.setPostcode("ABC 123");
        req.setAddress(address);


        String requestJson = objectMapper.writeValueAsString(req);
        given(userService.createUser(any())).willReturn(user);

        ResultActions resultActions = mockMvc.perform(
                post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );
        // Assert the status
        resultActions.andExpect(status().isBadRequest());
    }

}