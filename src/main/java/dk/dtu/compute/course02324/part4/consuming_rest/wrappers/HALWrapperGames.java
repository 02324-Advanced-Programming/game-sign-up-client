package dk.dtu.compute.course02324.part4.consuming_rest.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HALWrapperGames {

    @JsonProperty("_embedded")
    private Embedded embedded;

    @JsonCreator
    public HALWrapperGames(List<Game> games) {
        this.embedded = new Embedded(games);
    }

    public HALWrapperGames() {}

    public List<Game> getGames() {
        if (embedded != null && embedded.games != null) {
            return embedded.games;
        }
        return List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Embedded {
        @JsonProperty("games")
        public List<Game> games;

        public Embedded() {}
        public Embedded(List<Game> games) { this.games = games; }
    }
}
