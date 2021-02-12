package net.feliperocha.gameofthree.service;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.configuration.GameConfig;
import net.feliperocha.gameofthree.listener.dto.GameMessageDTO;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.domain.*;
import net.feliperocha.gameofthree.listener.dto.StartGameDTO;
import net.feliperocha.gameofthree.repository.GameRepository;
import net.feliperocha.gameofthree.repository.PlayerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static net.feliperocha.gameofthree.domain.GameStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.DISCONNECTED;
import static net.feliperocha.gameofthree.domain.PlayerStatus.FINISHED;
import static net.feliperocha.gameofthree.listener.GameEvent.*;

@Service
@AllArgsConstructor
public class GameService {
    private final GameConfig gameConfig;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String PLAYER_SUBSCRIBE_PATH = "/queue/player/%s";

    public void connect(String playerId) {
        var player = playerRepository.findById(playerId).orElseThrow();
        var game = gameRepository.findByStatus(WAITING_PLAYERS).stream()
                .filter(g -> g.getPlayers().size() < gameConfig.getNumberOfPlayers())
                .min(comparing(Game::getCreatedAt)).orElse(new Game());
        game.getPlayers().add(player);
        notifyNewPlayer(game);
        if (game.getPlayers().stream().filter(p -> p.getStatus().equals(WAITING)).count() == gameConfig.getNumberOfPlayers())
            game.startGame();
        game = gameRepository.save(game);
        messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, player.getId()),
                new GameMessageDTO(SUBSCRIBED, game.getId()));
        if (game.getStatus().equals(RUNNING))
            notifyGameStartedForPlayers(game);
    }

    public void start(StartGameDTO startGameDTO) {
        var player = playerRepository.findById(startGameDTO.getPlayerId()).orElseThrow();
        var game = gameRepository.findById(startGameDTO.getGameId()).orElseThrow();
        game.setInitialNumber(startGameDTO.getInitialNumber());
        game = gameRepository.save(game);
        notifyInitialNumber(game, player);
        notifyNextGameTurn(game, player, game.getInitialNumber());
    }

    public void executeMove(MoveDTO moveDTO) {
        var game = gameRepository.findById(moveDTO.getGameId()).orElseThrow();
        var player = playerRepository.findById(moveDTO.getPlayerId()).orElseThrow();
        var move = game.executeMove(moveDTO, gameConfig.getDivisor(), gameConfig.getWinningNumber());
        game = gameRepository.save(game);
        notifyPlayerMove(game, player, move);
        switch (game.getStatus()) {
            case RUNNING:
                notifyNextGameTurn(game, player, move.getCurrentNumber());
                break;
            case FINISHED:
                notifyGameEnd(game, move.getCurrentNumber());
                break;
        }
    }

    public void disconnectPlayer(String playerId, String gameId) {
        var player = playerRepository.findById(playerId).orElseThrow();
        player.setStatus(DISCONNECTED);
        playerRepository.save(player);
        var game = gameRepository.findById(gameId).orElseThrow();
        if (!game.getStatus().equals(GameStatus.DISCONNECTED)) {
            game.disconnectGame();
            gameRepository.save(game);
            notifyGameDisconnect(game, player);
        }
    }

    private void notifyGameStartedForPlayers(Game game) {
        var starterPlayer = game.getFirstPlayer();
        messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, starterPlayer.getId()),
                new GameMessageDTO(START, game.getId(),"Game started, enter with a initial number"));
        var gameMessageDTO = new GameMessageDTO(WAIT,
                game.getId(), format("Game started, it's %s turn", starterPlayer.getName()));
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(starterPlayer.getId()) && player.getStatus().equals(PLAYING))
                .forEach(currentPlayer -> messagingTemplate
                        .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
    }

    private void notifyNewPlayer(Game game) {
        var newPlayer = game.getLastPlayer();
        if (!game.getStatus().equals(RUNNING))
            messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, newPlayer.getId()),
                    new GameMessageDTO(WAIT, game.getId(),"Waiting for players..."));
        var gameMessageDTO = new GameMessageDTO(WAIT, game.getId(), format("%s joined the game", newPlayer.getName()));
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(newPlayer.getId())
                        && player.getStatus().equals(WAITING))
                .forEach(currentPlayer -> messagingTemplate
                        .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
    }

    private void notifyPlayerMove(Game game, Player currentPlayer, Move move) {
        game.getPlayers()
                .stream()
                .filter(player -> !player.getStatus().equals(DISCONNECTED))
                .forEach(player -> messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, player.getId()),
                        new GameMessageDTO(WAIT, game.getId(),
                                format("%s performed a %s move on number %s, now the current number is %s",
                                        player.getId().equals(currentPlayer.getId()) ? "You" : currentPlayer.getName(),
                                        move.getCommand(), move.getPreviousNumber(), move.getCurrentNumber()),
                                move.getCurrentNumber()
                        ))
                );
    }

    private void notifyInitialNumber(Game game, Player currentPlayer) {
        game.getPlayers()
                .stream()
                .filter(player -> !player.getStatus().equals(DISCONNECTED))
                .forEach(player -> messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, player.getId()),
                        new GameMessageDTO(WAIT, game.getId(), format("%s choose %s as initial number",
                                player.getId().equals(currentPlayer.getId()) ? "You" : currentPlayer.getName(),
                                game.getInitialNumber()), game.getInitialNumber())
                ));
    }

    private void notifyNextGameTurn(Game game, Player player, Integer currentNumber) {
        var nextPlayer = game.getNextPlayer(player);
        messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, nextPlayer.getId()),
                new GameMessageDTO(TURN, game.getId(), "It's your turn!", currentNumber));
        var gameMessageDTO =
                new GameMessageDTO(WAIT, game.getId(), format("It's %s turn", nextPlayer.getName()), currentNumber);
        game.getPlayers()
                .stream()
                .filter(currentPlayer -> !player.getId().equals(nextPlayer.getId()) && player.getStatus().equals(PLAYING))
                .forEach(currentPlayer -> messagingTemplate
                        .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
    }

    private void notifyGameEnd(Game game, Integer currentNumber) {
        var winnerPlayer = game.getWinnerPlayer();
        if (winnerPlayer.isPresent()) {
            messagingTemplate.convertAndSend(format(PLAYER_SUBSCRIBE_PATH, winnerPlayer.get().getId()),
                    new GameMessageDTO(WON, game.getId(), "Congratulations you won the game!", currentNumber));
            var gameMessageDTO = new GameMessageDTO(LOST,
                    game.getId(), format("%s won the game!", winnerPlayer.get().getName()), currentNumber);
            game.getPlayers()
                    .stream()
                    .filter(player -> !player.getId().equals(winnerPlayer.get().getId()) && player.getStatus().equals(FINISHED))
                    .forEach(currentPlayer -> messagingTemplate
                            .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
        } else {
            var gameMessageDTO = new GameMessageDTO(DRAW, game.getId(),
                    format("Game ended in a draw, the current number is below %s!", gameConfig.getWinningNumber()), currentNumber);
            game.getPlayers()
                    .stream()
                    .filter(player -> player.getStatus().equals(FINISHED))
                    .forEach(currentPlayer -> messagingTemplate
                            .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
        }
    }

    private void notifyGameDisconnect(Game game, Player disconnectedPlayer) {
        var gameMessageDTO = new GameMessageDTO(PLAYER_DISCONNECTED, game.getId(),
                format("%s disconnected from the game!", disconnectedPlayer.getName()));
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(disconnectedPlayer.getId()))
                .forEach(currentPlayer -> messagingTemplate
                        .convertAndSend(format(PLAYER_SUBSCRIBE_PATH, currentPlayer.getId()), gameMessageDTO));
    }
}
