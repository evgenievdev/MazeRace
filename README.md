# MazeRace

## About

MazeRace is a simple 2D multiplayer maze-solving game, where a minimum of 2 and maximum of 4 players can race against a timer and eachother to see who can reach the finish line first and gather the most coins. Each player spawns on one of the corners of the rectangular maze and must reach the center of the maze where the finish is. The maze is populated with coins, which can be collected by any of the players. Additionally, each player is armed with a bomb trap which can be dropped anywhere. Once an enemy player walks over the trap, a percentage of their coins are taken away. 

The game has a Server and Client package. A Database is used to store player accounts. To play the game each player must have an account. 

The game is written entirely in Java without any external dependencies with the exception of a Database driver, needed for user authentication. The game uses multi-threading and sockets to handle client connections. 

## Features
- Custom GUI (8-bit style)
- 2D movable viewport
- Procedurally generated (single-solution) hybrid grid-mazes
- Character animation system
- Custom Audio/Image/Font libraries
- Database/Server/Client models
- Coin collection mechanism
- Trap placement mechanism

## Controls
### In-game
- **Arrow keys** to move player
- **Space** to drop trap 
### Menu
- **CTRL** to switch active input field (or simply use the mouse)
- **ENTER** to submit form data

## Usage
- Comes with usable **Client** and **Server**
- First start the Server. You should see the ServerIP:Port combination in the console and a message saying "Database accessed".
- Then start as many Client instances as you wish.
- To play, each player must register an account and login with it.
- Once the player has logged in, they join a queue (waiting queue)
- If the minimum number of players to create a lobby is reached (default = 4), the players in the queue are sent to a lobby.
- The lobby game parameters are randomly chosen by the server (level, maze complexity, time limit, etc)
- Once in the lobby, at least 2 players must press "READY" to start the game countdown timer (default = 10s)
- Once the countdown timer reaches 0 OR all players have pressed "READY", the game begins
- The winner is the player who can reach the finish line of the maze first and gather the most coins

### Server
- Start **server/Server.java**
  - By default, the Server Port is **8000**
  - You can speicfy a custom Port in the **Arguments** array in the main method (format: port)
  - Alternatively you can hardcode the Server Port by editing Server.java's main method:

```java
int defaultPort = 8000;
```

### Client
- Start **client/ClientApp.java**
  - By default, Server IP is **localhost** and Port is **8000**
  - You can specify a custom Server IP and Port in the **Arguments** array in the main method (format: ip port)
  - Alternatively you can hardcode the Server IP and Port number by editing ClientApp.java's main method:

```java
String host = "localhost";
int port = 8000;
```

### Database
- The game utilizes a relational database
- Uses PostgreSQL by default (Java PostgreSQL .jar driver included in project)
- In the **server/Server.java** file, you can change the credentials for the Database authentication
  - The connector is located in the Server constructor method:

```java
// DB Host , DB Name , DB User , DB Password
db = new Database( "localhost" , "mazerace" , "postgres" , "admin" );
```

- You will need to import the table structures. The code is written below (SQL): 
```sql
CREATE TABLE players
(
  id serial NOT NULL,
  username character varying(32) NOT NULL,
  password character varying(256) NOT NULL,
  "loggedIn" boolean NOT NULL DEFAULT false,
  "lastLogin" timestamp with time zone NOT NULL,
  "totalCoins" integer NOT NULL DEFAULT 0,
  "gamesPlayed" integer NOT NULL DEFAULT 0,
  "gamesFinished" integer NOT NULL DEFAULT 0,
  "gamesWon" integer NOT NULL DEFAULT 0,
  "totalPoints" integer NOT NULL DEFAULT 0,
  CONSTRAINT players_pkey PRIMARY KEY (id)
);
		
CREATE TABLE scores
(
  username character varying(32) NOT NULL,
  mazesize integer NOT NULL,
  level character varying(64) NOT NULL,
  completiontime integer NOT NULL DEFAULT 0,
  finishposition integer NOT NULL DEFAULT 0,
  score integer NOT NULL DEFAULT 0,
  coins integer NOT NULL DEFAULT 0,
  date timestamp with time zone NOT NULL,
  CONSTRAINT scores_pkey PRIMARY KEY (username)
);
```

## Preview
### Collage
![Preview](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/preview/collage.jpg)
### Included Level Designs
![Desert](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Desert/header.jpg)
![Forest](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Forest/header.jpg)
![Gardens](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Gardens/header.jpg)
![Winter](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Winter/header.jpg)
![Underworld](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Underworld/header.jpg)
![Dungeon](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Dungeon/header.jpg)
