package dk.dtu.compute.course02324.part4.consuming_rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;
import dk.dtu.compute.course02324.part4.consuming_rest.model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameSignUpClient extends Application {
    private static final String BASE_URL = "http://localhost:8080/roborally";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private User currentUser;
    private ListView<String> gameListView;
    private List<Game> games;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = createMenuBar();
        gameListView = new ListView<>();

        VBox root = new VBox(menuBar, gameListView);
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Game Sign-Up Client");
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        Menu userMenu = new Menu("User");
        MenuItem signUp = new MenuItem("Sign Up");
        MenuItem signIn = new MenuItem("Sign In");
        MenuItem signOut = new MenuItem("Sign Out");
        signUp.setOnAction(e -> signUpUser());
        signIn.setOnAction(e -> signInUser());
        signOut.setOnAction(e -> signOutUser());
        userMenu.getItems().addAll(signUp, signIn, signOut);

        Menu gameMenu = new Menu("Games");
        MenuItem showGames = new MenuItem("Show Open Games");
        MenuItem joinGame = new MenuItem("Join Selected Game");
        MenuItem leaveGame = new MenuItem("Leave Selected Game");
        MenuItem deleteGame = new MenuItem("Delete Selected Game");
        MenuItem startGame = new MenuItem("Start Selected Game");
        MenuItem createGame = new MenuItem("Create New Game");
        showGames.setOnAction(e -> updateGameList());
        joinGame.setOnAction(e -> joinSelectedGame());
        leaveGame.setOnAction(e -> leaveSelectedGame());
        deleteGame.setOnAction(e -> deleteSelectedGame());
        startGame.setOnAction(e -> startSelectedGame());
        createGame.setOnAction(e -> createGame());
        gameMenu.getItems().addAll(showGames, joinGame, leaveGame, deleteGame, startGame, createGame);

        return new MenuBar(userMenu, gameMenu);
    }

    private void signUpUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username to register:");
        Optional<String> result = dialog.showAndWait();
        if (currentUser != null) {
            showAlert("Error", "You are already signed in as " + currentUser.getName() + ", please sign out first.");
            return;
        }
        result.ifPresent(username -> {
            try {
                User user = new User();
                user.setUsername(username);
                ResponseEntity<User> resp = restTemplate.postForEntity(
                        BASE_URL + "/users", user, User.class);
                currentUser = resp.getBody();
                showAlert("Success", "Registered and signed in as " + currentUser.getName());
            } catch (RestClientException ex) {
                showAlert("Error", "Registration failed: " + ex.getMessage());
            }
        });
    }

    private void signInUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username to sign in:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            try {
                ResponseEntity<User> response = restTemplate.postForEntity(
                        BASE_URL + "/users/login?username=" + username,
                        null,
                        User.class
                );
                currentUser = response.getBody();
                showAlert("Success", "Signed in as " + currentUser.getName());
            } catch (Exception ex) {
                showAlert("Error", "Sign in failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void signOutUser() {
        if (currentUser == null) {
            showAlert("Error", "No user is currently signed in.");
            return;
        }
        try {
            restTemplate.postForEntity(
                    BASE_URL + "/users/logout", currentUser, Void.class);
            showAlert("Success", "Signed out " + currentUser.getName());
            currentUser = null;
        } catch (Exception ex) {
            showAlert("Error", "Sign out failed: " + ex.getMessage());
        }
    }

    private void updateGameList() {
        Platform.runLater(() -> {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        BASE_URL + "/games/opengames", String.class);
                games = parseList(response.getBody(), new TypeReference<List<Game>>() {
                });
                gameListView.getItems().setAll(games.stream()
                        .map(g -> formatGameString(g))
                        .collect(Collectors.toList()));
            } catch (Exception ex) {
                showAlert("Error", "Could not fetch games: " + ex.getMessage());
            }
        });
    }

    private String formatGameString(Game g) {
        int joined = g.getPlayers() != null ? g.getPlayers().size() : 0;
        return String.format("[%d] %s (%d/%d) owner=%s",
                g.getUid(), g.getName(), joined, g.getMaxPlayers(), g.getOwner().getName());
    }

    private Game getSelectedGame() {
        int idx = gameListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || games == null || idx >= games.size()) return null;
        return games.get(idx);
    }

    private void createGame() {
        if (currentUser == null) {
            showAlert("Error", "You must be signed in to create a game.");
            return;
        }
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Create New Game");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Game Name");
        TextField minField = new TextField();
        minField.setPromptText("Min Players");
        TextField maxField = new TextField();
        maxField.setPromptText("Max Players");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Min:"), 0, 1);
        grid.add(minField, 1, 1);
        grid.add(new Label("Max:"), 0, 2);
        grid.add(maxField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == createButtonType) {
                try {
                    Game g = new Game();
                    g.setName(nameField.getText());
                    g.setMinPlayers(Integer.parseInt(minField.getText()));
                    g.setMaxPlayers(Integer.parseInt(maxField.getText()));
                    g.setOwner(currentUser);
                    return g;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Player counts must be numbers.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(game -> {
            try {
                restTemplate.postForEntity(BASE_URL + "/games", game, Game.class);
                showAlert("Success", "Game created: " + game.getName());
                updateGameList();
            } catch (RestClientException ex) {
                showAlert("Error", "Failed to create game: " + ex.getMessage());
            }
        });
    }

    private void joinSelectedGame() {
        Game g = getSelectedGame();
        if (g == null) {
            showAlert("Error", "No game selected");
            return;
        }
        if (g.getOwner().getUid() == currentUser.getUid()) {
            showAlert("Error", "Owner cannot join its own game");
            return;
        }
        if (g.getPlayers().size() >= g.getMaxPlayers()) {
            showAlert("Error", "Game is full");
            return;
        }
        try {
            // Send join request via dedicated endpoint, no body
            restTemplate.postForEntity(
                    BASE_URL + "/games/" + g.getUid() + "/join?userId=" + currentUser.getUid() + "&name=" + currentUser.getName(),
                    null,
                    Player.class
            );
            showAlert("Success", "Joined game " + g.getName());
            updateGameList();
        } catch (RestClientException ex) {
            ex.printStackTrace();
            showAlert("Error", "Join failed: " + ex.getMessage());
        }
    }


    private void leaveSelectedGame() {
        Game g = getSelectedGame();
        if (g == null) {
            showAlert("Error", "No game selected");
            return;
        }
        Optional<Player> me = g.getPlayers().stream()
                .filter(p -> p.getUser().getUid() == currentUser.getUid()).findFirst();
        if (me.isEmpty()) {
            showAlert("Error", "You are not in that game");
            return;
        }
        long pid = me.get().getUid();
        try {
            restTemplate.delete(BASE_URL + "/players/" + pid);
            showAlert("Success", "Left game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            showAlert("Error", "Leave failed: " + ex.getMessage());
        }
    }

    private void deleteSelectedGame() {
        Game g = getSelectedGame();
        if (g == null) {
            showAlert("Error", "No game selected");
            return;
        }
        if (g.getOwner().getUid() != currentUser.getUid()) {
            showAlert("Error", "Only owner can delete");
            return;
        }
        try {
            restTemplate.delete(BASE_URL + "/games/" + g.getUid() + "?userId=" + currentUser.getUid());
            showAlert("Success", "Deleted game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            showAlert("Error", "Delete failed: " + ex.getMessage());
        }
    }

    private void startSelectedGame() {
        Game g = getSelectedGame();
        if (g == null) {
            showAlert("Error", "No game selected");
            return;
        }
        if (g.getOwner().getUid() != currentUser.getUid()) {
            showAlert("Error", "Only owner can start");
            return;
        }
        if (g.getPlayers().size() < g.getMinPlayers()) {
            showAlert("Error", "Not enough players to start");
            return;
        }
        try {
            restTemplate.put(BASE_URL + "/games/" + g.getUid() + "/start" + "?userId=" + currentUser.getUid(), null);
            showAlert("Success", "Started game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Start failed: " + ex.getMessage());
        }
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> typeRef) throws Exception {
        JsonNode root = mapper.readTree(json);
        if (root.isArray()) {
            return mapper.readValue(json, typeRef);
        } else if (root.has("_embedded")) {
            JsonNode embedded = root.get("_embedded");
            if (embedded.has("games")) {
                return mapper.readValue(embedded.get("games").toString(), typeRef);
            } else if (embedded.has("users")) {
                return mapper.readValue(embedded.get("users").toString(), typeRef);
            }
        }
        return List.of();
    }

    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
