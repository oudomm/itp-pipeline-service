package dev.oudom.pipeline.client;

import dev.oudom.pipeline.client.dto.UserResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface JsonPlaceholderClient {

    // https://jsonplaceholder.typicode.com/users
    @GetExchange("/users")
    List<UserResponse> getUsers();

}
