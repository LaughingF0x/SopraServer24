package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Date;
/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUser(Long id){
        User foundUser= this.userRepository.findById(id).orElse(null);
        if (foundUser==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The user you searched for doesn't exist");
        }
        return foundUser;
    }

    public User createUser(User newUser) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.OFFLINE);

        checkIfUserExists(newUser);
        newUser.setStatus(UserStatus.ONLINE);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        User userByName = userRepository.findByName(userToBeCreated.getName());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
    }

    public User checkUser(User LoginUser) {
        User userByToken = null;
        User us = userRepository.findByUsername(LoginUser.getUsername()); //get all users

        // Check if user is null
        if (us == null) {
            // Handle the case where the user is not found
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username does not exist");
        }

        if (us.getUsername().equals(LoginUser.getUsername())) {
            userByToken = us;
        }

        String password = LoginUser.getPassword();


        boolean valid = userByToken != null && userByToken.getPassword().equals(password);
        //check if the password is the same by token
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "False Username or password");
        } //if it's not the case we throw an exception
        userByToken.setStatus(UserStatus.ONLINE); //setting the user online if everything is correct
        User EntityUser = userRepository.save(userByToken);
        userRepository.flush();

        return EntityUser;
    }
    public void logoutUser(Long UserId) {
        User userByID = null;

        List<User> usersByUsername = userRepository.findAll();

        for (User user : usersByUsername) {
            if (user.getId().equals(UserId)) {
                userByID = user;
            }
        }
        if (userByID == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userByID.setStatus(UserStatus.OFFLINE);
        userRepository.save(userByID);
        userRepository.flush();

    }
    public void updateUser(User updatedUser, Long id){
        User foundUser= getUser(id);
//        if (!foundUser.getToken().equals(updatedUser.getToken())){
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"the tokens don't match you cannot update your toekn"+ updatedUser.getToken());
//        }

        if (!updatedUser.getUsername().equals(foundUser.getUsername())){
            checkIfUserExists(updatedUser);
        }
        if (!Objects.equals(updatedUser.getUsername(), "")){
            foundUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getBirthdate()!=null){
            foundUser.setBirthdate(updatedUser.getBirthdate());
        }
    }
    public boolean verifyToken(String token) {
        User user = userRepository.findByToken(token);
        return user != null;
    }
}
