package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Games {

    private List<Game> game;

    public List<Game> getGame() {
        return game;
    }

    public void setGame(List<Game> game) {
        this.game = game;
    }

}
