package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import dk.dtu.compute.course02324.part4.consuming_rest.model.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HALWrapperUsers {

    @JsonProperty("_embedded")
    private EmbeddedUsers embedded;

    @JsonCreator
    public HALWrapperUsers(List<User> users) {
        this.embedded = new EmbeddedUsers(users);
    }

    public HALWrapperUsers() {}

    public List<User> getUsers() {
        return embedded != null && embedded.users != null ? embedded.users : List.of();
    }

    @JsonProperty("_embedded")
    public void setEmbedded(EmbeddedUsers embedded) {
        this.embedded = embedded;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmbeddedUsers {
        @JsonProperty("users")
        public List<User> users;

        public EmbeddedUsers() {}
        public EmbeddedUsers(List<User> users) { this.users = users; }
    }
}
