package dk.dtu.compute.course02324.part4.consuming_rest.Controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;
import dk.dtu.compute.course02324.part4.consuming_rest.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.util.List;

public class ServerController {
    private static final String BASE_URL = "http://localhost:8080/roborally";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public ServerController() {
        this.restTemplate = new RestTemplate();
        this.mapper = new ObjectMapper();
    }

    public User signUpUser(String username) {
        User user = new User();
        user.setUsername(username);
        ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/users", user, User.class);
        return response.getBody();
    }

    public User signInUser(String username) {
        ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/users/login?username=" + username, null, User.class);
        return response.getBody();
    }

    public void signOutUser(User user) {
        restTemplate.postForEntity(BASE_URL + "/users/logout", user, Void.class);
    }

    public List<Game> getOpenGames() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/games/opengames", String.class);
        return mapper.readValue(response.getBody(), new TypeReference<List<Game>>() {});
    }
    public void joinGame(User currentUser, Game game) {
        restTemplate.postForEntity(
                BASE_URL + "/games/" + game.getUid() + "/join?userId=" + currentUser.getUid() + "&name=" + currentUser.getName(),
                null,
                Void.class
        );
    }

    public void leaveGame(User currentUser, Game game) {
        restTemplate.delete(BASE_URL + "/games/" + game.getUid() + "/leave" + "?userId=" + currentUser.getUid());
    }

    public void deleteGame(User currentUser, Game game) {
        restTemplate.delete(BASE_URL + "/games/" + game.getUid() + "?userId=" + currentUser.getUid());
    }

    public void startGame(User currentUser, Game game) {
        restTemplate.put(BASE_URL + "/games/" + game.getUid() + "/start" + "?userId=" + currentUser.getUid(), null);
    }

    public ResponseEntity<Game> createGame(Game game) {
        return restTemplate.postForEntity(BASE_URL + "/games", game, Game.class);
    }
}
