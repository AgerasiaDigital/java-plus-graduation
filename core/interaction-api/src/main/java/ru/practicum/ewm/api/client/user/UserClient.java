package ru.practicum.ewm.api.client.user;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.api.client.user.dto.UserDto;

@FeignClient(name = "user-service", path = "/internal/users", configuration = UserClientConfiguration.class)
public interface UserClient {

    /**
     * Fetches a list of users based on their IDs.
     * This method is essential for bulk enrichment operations.
     * @param ids The list of user IDs to retrieve.
     * @return A List of UserDto objects, each containing user details.
     */
    @GetMapping("/by-ids")
    List<UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids);

    /**
     * Fetches details for a single user by their IDs.
     * @param id The ID of the user to retrieve.
     * @return The UserDto object containing the user's details.
     */
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    /**
     * Performs an efficient existence check for a user by their ID.
     * This method typically returns a 2xx status code if the user exists
     * and a 404 status code if the user does not exist, without returning a body.
     * @param id The ID of the user to check for existence.
     */
    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    void checkUserExists(@PathVariable("id") Long id);
}