package dk.dtu.compute.course02324.part4.consuming_rest.View;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dk.dtu.compute.course02324.part4.consuming_rest.Controller.ServerController;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;
import dk.dtu.compute.course02324.part4.consuming_rest.model.User;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameSignUpClient extends Application {
    private ServerController serverController = new ServerController();
    private Label currentUserLabel;
    private User currentUser;
    private ListView<String> gameListView;
    private List<Game> games;

    // Menu Items for User actions
    private MenuItem signUp;
    private MenuItem signIn;
    private MenuItem signOut;

    // Menu Items for Game actions
    private MenuItem showGames;
    private MenuItem joinGame;
    private MenuItem leaveGame;
    private MenuItem deleteGame;
    private MenuItem startGame;
    private MenuItem createGame;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = createMenuBar();
        gameListView = new ListView<>();
        currentUserLabel = new Label("Not signed in");
        currentUserLabel.setPadding(new Insets(5));

        // Add a listener for when a game is selected
        gameListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateGameMenuItems();
        });

        VBox root = new VBox(menuBar, currentUserLabel, gameListView);
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Game Sign-Up Client");
        primaryStage.show();

        updateSignMenuItems();
    }

    private MenuBar createMenuBar() {
        // User Menu
        Menu userMenu = new Menu("User");
        signUp = new MenuItem("Sign Up");
        signIn = new MenuItem("Sign In");
        signOut = new MenuItem("Sign Out");
        signUp.setOnAction(e -> signUpUser());
        signIn.setOnAction(e -> signInUser());
        signOut.setOnAction(e -> signOutUser());
        userMenu.getItems().addAll(signUp, signIn, signOut);

        // Game Menu
        Menu gameMenu = new Menu("Games");
        showGames = new MenuItem("Show Open Games");
        joinGame = new MenuItem("Join Selected Game");
        leaveGame = new MenuItem("Leave Selected Game");
        deleteGame = new MenuItem("Delete Selected Game");
        startGame = new MenuItem("Start Selected Game");
        createGame = new MenuItem("Create New Game");
        showGames.setOnAction(e -> updateGameList());
        joinGame.setOnAction(e -> joinSelectedGame());
        leaveGame.setOnAction(e -> leaveSelectedGame());
        deleteGame.setOnAction(e -> deleteSelectedGame());
        startGame.setOnAction(e -> startSelectedGame());
        createGame.setOnAction(e -> createGame());
        gameMenu.getItems().addAll(showGames, joinGame, leaveGame, deleteGame, startGame, createGame);

        return new MenuBar(userMenu, gameMenu);
    }

    // Update sign in/up menu items based on currentUser state
    private void updateSignMenuItems() {
        boolean signedIn = (currentUser != null);
        signUp.setDisable(signedIn);
        signIn.setDisable(signedIn);
        signOut.setDisable(!signedIn);

        // Disable game options if not signed in
        joinGame.setDisable(!signedIn);
        leaveGame.setDisable(!signedIn);
        deleteGame.setDisable(!signedIn);
        startGame.setDisable(!signedIn);
        createGame.setDisable(!signedIn);
    }

    private void signUpUser() {
        signUpUser(null);
    }

    private void signUpUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Enter username to register:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                username = result.get();
            } else {
                return;
            }
        }
        try {
            currentUser = serverController.signUpUser(username);
            updateCurrentUserLabel();
            showAlert("Success", "Registered and signed in as " + currentUser.getName());
        } catch (Exception ex) {
            showAlert("Error", "Registration failed: " + ex.getMessage());
        }
    }

    private void signInUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username to sign in:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            try {
                currentUser = serverController.signInUser(username);
                updateCurrentUserLabel();
                updateGameMenuItems();
                showAlert("Success", "Signed in as " + currentUser.getName());
            } catch (Exception ex) {
                showAlert("Error", "Sign in failed: " + ex.getMessage());
            }
        });
    }

    private void signOutUser() {
        try {
            serverController.signOutUser(currentUser);
            showAlert("Success", "Signed out " + currentUser.getName());
            currentUser = null;
            updateCurrentUserLabel();
        } catch (Exception ex) {
            showAlert("Error", "Sign out failed: " + ex.getMessage());
        }
    }

    private void updateCurrentUserLabel() {
        if (currentUser != null) {
            currentUserLabel.setText("Signed in as: " + currentUser.getName());
        } else {
            currentUserLabel.setText("Not signed in");
        }
        updateSignMenuItems();
        updateGameMenuItems();
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
            serverController.joinGame(currentUser, g);
            showAlert("Success", "Joined game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            showAlert("Error", "Join failed: " + ex.getMessage());
        }
    }

    private void leaveSelectedGame() {
        Game g = getSelectedGame();
        if (g == null) {
            showAlert("Error", "No game selected");
            return;
        }
        if (g.getOwner().getUid() == currentUser.getUid()) {
            showAlert("Error", "You cannot leave a game you created. You must delete it instead.");
            return;
        }
        if (g.getPlayers().size() >= g.getMaxPlayers()) {
            showAlert("Error", "You cannot leave the game because it is full. Game is starting shortly.");
            return;
        }
        try {
            serverController.leaveGame(currentUser, g);
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
            serverController.deleteGame(currentUser, g);
            showAlert("Success", "Deleted game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            showAlert("Error", "Delete failed: " + ex.getMessage());
        }
    }

    private void startSelectedGame() {
        Game g = getSelectedGame();
        if (currentUser == null) {
            showAlert("Error", "You must be signed in to start a game.");
            return;
        }
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
            serverController.startGame(currentUser, g);
            showAlert("Success", "Started game " + g.getName());
            updateGameList();
        } catch (Exception ex) {
            showAlert("Error", "Start failed: " + ex.getMessage());
        }
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
                serverController.createGame(game);
                showAlert("Success", "Game created: " + game.getName());
                updateGameList();
            } catch (Exception ex) {
                showAlert("Error", "Failed to create game: " + ex.getMessage());
            }
        });
    }

    private void updateGameList() {
        Platform.runLater(() -> {
            try {
                games = serverController.getOpenGames();
                gameListView.getItems().setAll(
                        games.stream().map(g -> formatGameString(g)).collect(Collectors.toList())
                );
            } catch (Exception ex) {
                showAlert("Error", "Could not fetch games: " + ex.getMessage());
            }
        });
    }

    private String formatGameString(Game g) {
        int joined = g.getPlayers() != null ? g.getPlayers().size() : 0;
        String playerNames =(g.getPlayers() != null && !g.getPlayers().isEmpty())
                ? g.getPlayers().stream()
                .map(player->player.getName())
                .collect(Collectors.joining(", "))
                : "";

        return String.format("[%d] %s (%d/%d) host : %s / joined players : %s",
                g.getUid(), g.getName(), joined, g.getMaxPlayers(), g.getOwner().getName(), playerNames);
    }

    // Returns the currently selected Game object based on the list view.
    private Game getSelectedGame() {
        int idx = gameListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || games == null || idx >= games.size()) return null;
        return games.get(idx);
    }

    private void updateGameMenuItems() {
        boolean signedIn = (currentUser != null);
        signUp.setDisable(signedIn);
        signIn.setDisable(signedIn);
        signOut.setDisable(!signedIn);
        joinGame.setDisable(!signedIn);
        leaveGame.setDisable(!signedIn);
        deleteGame.setDisable(!signedIn);
        startGame.setDisable(!signedIn);
        createGame.setDisable(!signedIn);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
