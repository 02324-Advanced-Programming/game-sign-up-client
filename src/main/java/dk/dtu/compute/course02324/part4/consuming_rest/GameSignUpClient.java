package dk.dtu.compute.course02324.part4.consuming_rest;

import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;
import dk.dtu.compute.course02324.part4.consuming_rest.wrappers.HALWrapperGames;
import dk.dtu.compute.course02324.part4.consuming_rest.wrappers.HALWrapperPlayers;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

public class GameSignUpClient extends Application {

    private final RestClient client = RestClient.builder().baseUrl("http://localhost:8080").build();
    private VBox gameListBox;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Game Sign-up");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Button addGameButton = new Button("Add new game...");
        addGameButton.setOnAction(e -> showAddGameDialog());
        root.getChildren().add(addGameButton);

        Button openGamesButton = new Button("Open games...");
        openGamesButton.setOnAction(e -> showOpenGamesDialog());
        root.getChildren().add(openGamesButton);

        gameListBox = new VBox(10);
        updateGameList();
        root.getChildren().add(gameListBox);

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateGameList() {
        gameListBox.getChildren().clear();
        List<Game> games = client.get().uri("/game").retrieve().body(HALWrapperGames.class).getGames();

        for (Game game : games) {
            VBox gameBox = new VBox(5);
            gameBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");

            Label name = new Label("Game: " + game.getName());
            Label minPlayers = new Label("Min players: " + game.getMinPlayers());
            Label maxPlayers = new Label("Max players: " + game.getMaxPlayers());
            Label state = new Label("State: " + game.getState());
            //Label creator = new Label("Creator: " + game.getCreator().getName());
            //gameBox.getChildren().addAll(name, minPlayers, maxPlayers, state);
            Button signUpButton = new Button("← Sign up");
            signUpButton.setOnAction(e -> showSignUpDialog(game));

            List<Player> players = client.get().uri("/game/" + game.getUid() + "/players").retrieve().body(HALWrapperPlayers.class).getPlayers();
            ListView<String> playersListView = new ListView<>();
            for(var p : players){
                playersListView.getItems().add(p.getName());
            }

            gameBox.getChildren().addAll(name, minPlayers, maxPlayers, state, signUpButton,playersListView);
            gameListBox.getChildren().add(gameBox);
        }
    }

    private void showAddGameDialog() {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Add New Game");

        TextField nameField = new TextField();
        TextField minField = new TextField();
        TextField maxField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Min players:"), 0, 1); grid.add(minField, 1, 1);
        grid.add(new Label("Max players:"), 0, 2); grid.add(maxField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                Game newGame = new Game();
                newGame.setName(nameField.getText());
                newGame.setMinPlayers(Integer.parseInt(minField.getText()));
                newGame.setMaxPlayers(Integer.parseInt(maxField.getText()));
                newGame.setState("OPEN");
                return newGame;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(game -> {
            client.post().uri("/game").body(game).retrieve().toBodilessEntity();
            updateGameList();
        });
    }

    private void showOpenGamesDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Show Online Games");

        VBox gamesBox = new VBox(10);
        gamesBox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(gamesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        try {
            // Get all games first
            HALWrapperGames wrapper = client.get().uri("/game").retrieve().body(HALWrapperGames.class);

            if (wrapper == null || wrapper.getGames() == null) {
                gamesBox.getChildren().add(new Label("Error loading games from server"));
                System.out.println("Error: Received null from API");
            } else {
                // Filter open games
                List<Game> openGames = wrapper.getGames().stream()
                        .filter(game -> "OPEN".equals(game.getState()))
                        .toList();

                System.out.println("Total games: " + wrapper.getGames().size());
                System.out.println("Open games: " + openGames.size());

                if (openGames.isEmpty()) {
                    gamesBox.getChildren().add(new Label("No open games available"));
                } else {
                    for (Game game : openGames) {
                        VBox gameBox = new VBox(5);
                        gameBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");

                        Label name = new Label("Game: " + game.getName());
                        Label minPlayers = new Label("Min players: " + game.getMinPlayers());
                        Label maxPlayers = new Label("Max players: " + game.getMaxPlayers());
                        Label state = new Label("State: " + game.getState());

                        Button signUpButton = new Button("Sign up for this game");
                        signUpButton.setOnAction(e -> {
                            dialog.close();
                            showSignUpDialog(game);
                        });

                        gameBox.getChildren().addAll(name, minPlayers, maxPlayers, state, signUpButton);
                        gamesBox.getChildren().add(gameBox);
                    }
                }
            }
        } catch (Exception e) {
            gamesBox.getChildren().add(new Label("Error loading games: " + e.getMessage()));
            e.printStackTrace();
        }

        dialog.getDialogPane().setContent(scrollPane);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        dialog.showAndWait();
    }

    private void showSignUpDialog(Game game) {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Sign Up to Game");

        TextField playerNameField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Game: " + game.getName()), 0, 0, 2, 1);
        grid.add(new Label("Player name:"), 0, 1); grid.add(playerNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType signUpButton = new ButtonType("Sign up!", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(signUpButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == signUpButton) {
                Player newPlayer = new Player();
                newPlayer.setName(playerNameField.getText());
                return newPlayer;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(player -> {
            // First, create the player with just a POST request
            ResponseEntity<Void> response = client.post()
                    .uri("/player")
                    .body(player)
                    .retrieve()
                    .toBodilessEntity();

            // get Location header, which contains the URL of the created player
            //i had to do it like this bc idk why but trying to get it directly from the response object didn't work
            String location = response.getHeaders().getLocation().toString();
            System.out.println("Created player location: " + location);

            // get the player ID from the location URL
            String playerId = location.substring(location.lastIndexOf("/")+1);
            player.setUid(Long.parseLong(playerId));

            System.out.println("Player created with ID: " + player.getUid());
            System.out.println("Player created: " + player.toString());


        // the following put request will connect game1 with player1:

        String body = "http://localhost:8080/game/" + game.getUid();
        String uri = "/player/" + playerId + "/game";


        ResponseEntity<Player> playerResponseEntity = client.put().uri(uri).
                header("Content-Type", "text/uri-list").
                body(body).retrieve().toEntity(Player.class);


            updateGameList();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}


//package dk.dtu.compute.course02324.part4.consuming_rest;
//
//import dk.dtu.compute.course02324.part4.consuming_rest.wrappers.HALWrapperGames;
//import dk.dtu.compute.course02324.part4.consuming_rest.model.Game;
//import dk.dtu.compute.course02324.part4.consuming_rest.model.Player;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestClient;
//
//import java.util.List;
//
//
////public class GameSignUpClient {
////
////    // see https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-resttemplate
////
////    public static void main(String[] args) {
////
////        /* Before you start this make sure you have created a player (with uid=1) and
////         * a game (with uid=1) in the database; you can do that via the command
////         * line tool curl, the tool Postman or  the HAL explorer (which, after
////         * your have started your backend is available at http://localhost:8080/api).
////         *
////         * You can create a player by posting
////         *
////         *   {
////         *     "maxPlayers": 2,
////         *     "minPlayers": 6,
////         *     "name": "First Game"
////         *    }
////         *
////         * to http://localhost:8080/game
////         *
////         * and by posting
////         *
////         *   {
////         *     "name": "Player 1"
////         *   }
////         *
////         * to ttp://localhost:8080/game
////         *
////         */
////
////        RestClient customClient = RestClient.builder().
////                // requestFactory(new HttpComponentsClientHttpRequestFactory()).
////                baseUrl("http://localhost:8080").
////                build();
////
////        // String result = customClient.get().uri("/game").retrieve().body(String.class);
////        String result = customClient.get().uri("/").retrieve().body(String.class);
////
////        System.out.println(result);
////
////        System.out.println("---------------------------------------------------------");
////
////        result = customClient.get().uri("/game").retrieve().body(String.class);
////
////        System.out.println(result);
////
////        System.out.println("---------------------------------------------------------");
////
////
////        Game game1 = customClient.get().uri("/game/1").retrieve().body(Game.class);
////
////        System.out.println("Game with uid 1 is: " + game1);
////
////        System.out.println("---------------------------------------------------------");
////
////        List<Game> games = customClient.get().uri("/game").retrieve().body(HALWrapperGames.class).getGames();
////
////        for (Game game: games) {
////            System.out.println(game);
////        }
////
////        System.out.println("---------------------------------------------------------");
////
////        Player player1 = customClient.get().uri("/player/1").retrieve().body(Player.class);
////
////        System.out.println("Player with uid 2 is: " + player1);
////
////
////        System.out.println("---------------------------------------------------------");
////
////        // the following put request will connect game1 with player1:
////
////        String body = "http://localhost:8080/game/1";
////
////        ResponseEntity<Player> playerResponseEntity = customClient.put().uri("/player/1/game").
////                header("Content-Type", "text/uri-list").
////                body(body).retrieve().toEntity(Player.class);
////        System.out.println("player: " + playerResponseEntity.toString());
////
////
////        System.out.println("---------------------------------------------------------");
////
////        game1 = customClient.get().uri("/player/1/game").retrieve().body(Game.class);
////
////        System.out.println("Game attached to Player with uid 1 is: " + game1);
////
////
////        // TODO try to read out the available games from the backend, show them on a
////        //      simple graphical GUI and sign up for a game using some of the operations
////        //      at the top.
////        //      For the GUI to work in JavaFX, you need to add some maven dependencies
////        //      (see pom file for Assignment 3).
////
////    }
//}
