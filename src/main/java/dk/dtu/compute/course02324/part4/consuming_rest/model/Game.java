package dk.dtu.compute.course02324.part4.consuming_rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

    private long uid;
    private String name;
    private int minPlayers;
    private int maxPlayers;

    private GameState state;
    private User owner;

    private List<Player> players;

    private boolean isOpen;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public User getCreator() {
        return owner;
    }

    public void setCreator(User creator) {
        this.owner = creator;
    }

    @Override
    public String toString() {
        return "Game{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", owner=" + (owner != null ? owner.getName() : "null") +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", isOpen=" + isOpen +
                '}';
    }
}
