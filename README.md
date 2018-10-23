# MazeRace

## About

## Usage
- Comes with usable **Client** and **Server**
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
### Level Design
![Desert](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Desert/header.jpg)
![Forest](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Forest/header.jpg)
![Gardens](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Gardens/header.jpg)
![Winter](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Winter/header.jpg)
![Underworld](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Underworld/header.jpg)
![Dungeon](https://raw.githubusercontent.com/evgenievdev/MazeRace/master/src/client/Data/Textures/Levels/Dungeon/header.jpg)
