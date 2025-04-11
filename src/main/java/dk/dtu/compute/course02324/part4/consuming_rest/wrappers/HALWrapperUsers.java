package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import dk.dtu.compute.course02324.part4.consuming_rest.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HALWrapperUsers {

    private Users _embedded;

    public Users get_embedded() {
        return _embedded;
    }

    public void set_embedded(Users _embedded) {
        this._embedded = _embedded;
    }

    public List<User> getUsers() {
        if (_embedded != null && _embedded.getUser() != null) {
            return _embedded.getUser();
        }

        return null;
    }


}
