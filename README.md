For the GUI everything is handles in two seperate drop-down menus. One for user sign in/up/out and another for any game interaction (showing, creating, deleting,  joining, leaving, starting). There is a clear representation if you haven’t signed in yet. And if you have signed in, you can see what user you are signed in as.


Sign up:

A sign up option in the menu bar allows users to register. When clicked a popup window appears asking for a username. If the user confirms the sign up, the client will update. It sends a http request to the UserController on the server. The server’s UserController calls UserService.createUser() to store the user and returns the uid.
In the client the client view takes care of the visuals, while the ServerController sends the request.

Sign in/out

Users can sign in in the menu bar ’user’. A pop up will promt for a username, if a matching user is found, the uid is stored. If not it promts if the user wishes to sign up with that username instead. A  user can also sign out, resetting their onlinestate in the backend.
Any game related actions in the ’Games’ menu dropdown will be disabled until signed in.

Viewing open games

The ’Show open games’ menu option in the ’games’ drop menu, retrieves all OPEN games from the server. It displays one game per line with the game name, min and max players, creator’s name (displayed as host). 
By clicking on a game and using the ’Games’ drop down menu users can join, leave, start and delete games(if they are the owner) button is unfortunately not disabled, but a message will let you know that you are not the creator of the game if you try and start or delete someone elses game. 

Creating a game

Users who are signed in can click ’create game’. A popup window appears, where you can customize the game (give it a name and a minimum / maximum og players that can join.
The game is posted to the server with the creator’s uid, where it is used to add the creator as a player. 

Joining a game 

A player cannot join a game if they are the creator of the game or if the game capacity is at max. A message will let you know if try. If a join is successfull the client creates a plaer for the game and posts it to the server.

Leaving a game 

Users can leave a game unless they are the creator or if the game is full. Unfortunately the button for leaving game is not disabled, but instead a popup will let you know and prompt you to delete the game instead if your the creator or wait fior the game to start if game is full.

Starting a game

Only a creator can start the game, and the game must have the required minimum of players in order to start. 
