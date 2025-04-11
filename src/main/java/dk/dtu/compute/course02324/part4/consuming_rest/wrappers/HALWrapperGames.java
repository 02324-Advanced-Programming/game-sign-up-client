package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HALWrapperGames {

    private Games _embedded = null;

    public Games get_embedded() {
        return _embedded;
    }

    public void set_embedded(Games _embedded) {
        this._embedded = _embedded;
    }

    public List<Game> getGames() {
        if (_embedded != null && _embedded.getGame() != null) {
            return _embedded.getGame();
        }

        return null;
    }

}
