package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Players {

    @JsonProperty("player")
    private List<Player> playersList;

    public List<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<Player> playersList) {
        this.playersList = playersList;
    }


}
