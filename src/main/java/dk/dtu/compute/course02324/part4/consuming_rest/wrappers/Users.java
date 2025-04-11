package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;


import dk.dtu.compute.course02324.part4.consuming_rest.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Users {

    private List<User> user;

    public List<User> getUser() {
        return user;
    }

    public void setPlayer(List<User> user) {
        this.user = user;
    }

}
