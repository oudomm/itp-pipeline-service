package dev.oudom.pipeline.controller;

import dev.oudom.pipeline.client.JsonPlaceholderClient;
import dev.oudom.pipeline.client.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/jph")
@RequiredArgsConstructor
public class JsonPlaceholderClientController {

    private final JsonPlaceholderClient jsonPlaceholderClient;

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return jsonPlaceholderClient.getUsers();
    }

}
