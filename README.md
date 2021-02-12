# Game of Three
A game with 2 or more players trying to reach the winning number

## Description
The game starts when a player starts a game through the front-end app, a random number is generated and it's being sent to the second player. 
The receiving player can now always choose between adding one of {​ 1 (ADD), 0 (MAINTAIN), -1 (SUBTRACT)} to get to a number that is divisible by 3. Divide it by three. The resulting whole number is then sent back to the original sender. 
The same rules are applied until one player reaches the number 1 (after the division), which is the winning number.

_obs: you can change the values for divisor winning number and number of players through [env variables below](#environment-variables)_

## Application Stack
  * Java 11 (Zulu OpenJDK)
  * Gradle 6.8.x
  * Spring Boot 2.4.x
  * WebSocket with STOMP
  * MongoDB

## Environment Variables
The following variables should be set before running:

| Variable name | Default value |
|---------------|---------------|
| DATABASE_URL | mongodb://mongo:27017/game-of-three (docker) | |
| DIVISOR | 3 |
| WINNING_NUMBER | 1 |
| NUMBER_OF_PLAYERS | 2 |

## Running

1. Build docker-compose images: `docker-compose build`
2. Run docker-compose: `docker-compose up`
3. Open browser on [localhost:8000](http://localhost:8000)

It's also available on: [https://felipe-rocha-game-of-three.herokuapp.com](https://felipe-rocha-game-of-three.herokuapp.com)

### Running tests:
* Run: `./gradlew test`
