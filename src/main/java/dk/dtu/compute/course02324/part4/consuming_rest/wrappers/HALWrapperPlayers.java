package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HALWrapperPlayers {

    private Players _embedded;

    public Players get_embedded() {
        return _embedded;
    }

    public void set_embedded(Players _embedded) {
        this._embedded = _embedded;
    }

    public List<Player> getPlayers() {
        if (_embedded != null && _embedded.getPlayersList() != null) {
            return _embedded.getPlayersList();
        }

        return null;
    }


}
