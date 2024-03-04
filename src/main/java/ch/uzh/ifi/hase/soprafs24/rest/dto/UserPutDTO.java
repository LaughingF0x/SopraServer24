package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDate;
import java.util.Date;

public class UserPutDTO {

    private String username;
    private Date birthdate;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(Date birthdate){
        this.birthdate=birthdate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
