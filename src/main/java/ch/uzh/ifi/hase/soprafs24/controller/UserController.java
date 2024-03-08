package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TokenDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("/users/login") //used for logging in the user
    @ResponseStatus(HttpStatus.OK) //used post, there is no strict rule what to use put/post
    @ResponseBody
    public UserGetDTO loginTheUser(@RequestBody UserPostDTO userPostDTO){
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check credentials
        User EntityUser = userService.checkUser(userInput);

        //convert mappedUser back to API and return
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(EntityUser);
    }

    @GetMapping("/logout/{userId}") //for setting user offline
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logOutUser(@PathVariable("userId") Long userid){
        // this function is implemented in the UserService
        userService.logoutUser(userid);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        // create user
        User createdUser = userService.createUser(userInput);
        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }
    @GetMapping(value = "/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getAUser(@PathVariable("id") String id) {
        Long idLong = convertStringToLong(id);
        assert idLong!=null;
        User foundUser= userService.getUser(idLong);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(foundUser);
    }
    @PutMapping(value = "/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateUser(@RequestBody UserPutDTO userPutDTO, @PathVariable("id") String id){
        Long idLong=convertStringToLong(id);
        User user=DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
        userService.getUser(idLong);
        userService.updateUser(user, idLong);
    }
    private Long convertStringToLong(String id){
        Long idLong;
        try {
            idLong = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The id is not a number");
        }
        return idLong;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody TokenDTO tokenDTO) {
        boolean isValid = userService.verifyToken(tokenDTO.getToken());
        if (isValid) {
            return ResponseEntity.ok().body("Token is valid.");
        } else {
            return ResponseEntity.badRequest().body("Token is invalid.");
        }
    }
}
