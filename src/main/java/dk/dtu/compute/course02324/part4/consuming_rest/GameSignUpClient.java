package dk.dtu.compute.course02324.part4.consuming_rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
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

public class GameSignUpClient extends Application {
    private static final String BASE_URL = "http://localhost:8080/roborally";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private User currentUser;
    private ListView<String> gameListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = createMenuBar();
        gameListView = new ListView<>();
        Button createGameButton = new Button("Create New Game");
        createGameButton.setOnAction(e -> createGame());

        VBox root = new VBox(menuBar, gameListView, createGameButton);
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.setTitle("Game Sign-Up Client");
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        Menu userMenu = new Menu("User");
        MenuItem signUp = new MenuItem("Sign Up");
        signUp.setOnAction(e -> signUpUser());
        MenuItem signIn = new MenuItem("Sign In");
        signIn.setOnAction(e -> signInUser());
        userMenu.getItems().addAll(signUp, signIn);

        Menu gameMenu = new Menu("Games");
        MenuItem showGames = new MenuItem("Show Open Games");
        showGames.setOnAction(e -> updateGameList());
        gameMenu.getItems().add(showGames);

        return new MenuBar(userMenu, gameMenu);
    }

    private void signUpUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username to register:");
        dialog.showAndWait().ifPresent(username -> {
            try {
                User user = new User();
                user.setUsername(username);
                restTemplate.postForEntity(BASE_URL + "/users", user, User.class);
                showAlert("Success", "Registered user " + username);
            } catch (RestClientException ex) {
                ex.printStackTrace();
                showAlert("Error", "Registration failed: " + ex.getMessage());
            }
        });
    }

    private void signInUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username to sign in:");
        dialog.showAndWait().ifPresent(username -> {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        BASE_URL + "/users?username=" + username, String.class);
                List<User> users = parseList(response.getBody(), new TypeReference<List<User>>() {});
                if (!users.isEmpty()) {
                    currentUser = users.get(0);
                    showAlert("Success", "Signed in as " + username);
                } else {
                    showAlert("Error", "User not found");
                }
            } catch (Exception ex) {
                showAlert("Error", "Sign in failed: " + ex.getMessage());
            }
        });
    }

    private void updateGameList() {
        Platform.runLater(() -> {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        BASE_URL + "/games/opengames", String.class);
                List<Game> games = parseList(response.getBody(), new TypeReference<List<Game>>() {});
                gameListView.getItems().setAll(
                        games.stream()
                                .map(game -> game.getName()
                                        + " (" + game.getPlayers().size()
                                        + "/" + game.getMaxPlayers() + ")")
                                .toList()
                );
            } catch (Exception ex) {
                showAlert("Error", "Could not fetch games: " + ex.getMessage());
            }
        });
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
