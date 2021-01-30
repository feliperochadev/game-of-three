# Game of Three
A game with 2 players trying to reach the winning number

## Description
The game starts when a player starts a game through the front-end app, a random number is generated and it's being sent to the second player. 
The receiving player can now always choose between adding one of {​ 1 (addition), 0 (maintain), -1 (subtraction)} to get to a number that is divisible by 3. Divide it by three. The resulting whole number is then sent back to the original sender. 
The same rules are applied until one player reaches the number 1 (after the division), which is the winning number.

_obs: you can change the values for divisor and winning number through [env variables below](#environment-variables)_

## Application Stack
  * Java 11 (Zulu OpenJDK)
  * Gradle 6.8.x
  * Spring Boot 2.4.x

## Environment Variables
The following variables should be set before running:

| Variable name | Default value | Description |
|---------------|---------------| ------------|
|  |  | |

## Running

### Running project locally with Docker:
1. Build docker-compose images: `docker-compose build`
2. Run docker-compose: `docker-compose up`
3. Check if it's running `curl localhost:8080/health`

_optional: you can override the [env variables](#environment-variables) exporting them or using a .env file_

### Running tests:
* Run: `./gradlew clean test`
