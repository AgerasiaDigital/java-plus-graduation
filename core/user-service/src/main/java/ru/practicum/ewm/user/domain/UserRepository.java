package ru.practicum.ewm.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    /**
     * Saves a new user or updates an existing one.
     *
     * @param user The user to be saved.
     * @return The saved user.
     */
    User save(User user);

    /**
     * Finds a user by their ID.
     *
     * @param userId The ID of the user.
     * @return An {@link Optional} containing the user if found, or empty otherwise.
     */
    Optional<User> findById(Long userId);

    /**
     * Checks if a user with the given ID exists.
     *
     * @param id The ID to check.
     * @return {@code true} if a user with this ID exists, {@code false} otherwise.
     */
    boolean existsById(Long id);

    /**
     * Checks if a user with the given email already exists.
     *
     * @param email The email to check.
     * @return {@code true} if a user with this email exists, {@code false} otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by their ID.
     *
     * @param userId The ID of the user to delete.
     */
    void deleteById(Long userId);

    /**
     * Finds all users, with pagination.
     *
     * @param from The starting index of the users to return.
     * @param size The number of users to return.
     * @return A list of users.
     */
    List<User> findAll(int from, int size);

    /**
     * Finds all users whose IDs are in the given list.
     *
     * @param ids A list of user IDs to find.
     * @return A list of users matching the given IDs. The list can be empty if no users are found.
     */
    List<User> findByIdIn(List<Long> ids);

    /**
     * Finds all users whose IDs are in the given list, with pagination.
     *
     * @param ids  A list of user IDs to find.
     * @param from The starting index of the users to return.
     * @param size The number of users to return.
     * @return A list of users.
     */
    List<User> findAllByIdIn(List<Long> ids, int from, int size);
}