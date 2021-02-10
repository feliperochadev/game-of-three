package net.feliperocha.gameofthree.service;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.configuration.GameConfig;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.domain.*;
import net.feliperocha.gameofthree.repository.GameRepository;
import net.feliperocha.gameofthree.repository.PlayerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static net.feliperocha.gameofthree.domain.GameStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.FINISHED;
import static net.feliperocha.gameofthree.domain.PlayerStatus.PLAYING;
import static net.feliperocha.gameofthree.domain.PlayerStatus.WAITING;

@Service
@AllArgsConstructor
public class GameService {
    private final GameConfig gameConfig;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String START_GAME_SUBSCRIBE_PATH = "/queue/start";
    private static final String WAIT_SUBSCRIBE_PATH = "/queue/wait";
    private static final String ROUND_SUBSCRIBE_PATH = "/queue/round";
    private static final String WIN_SUBSCRIBE_PATH = "/queue/win";
    private static final String LOSE_SUBSCRIBE_PATH = "/queue/lose";

    public void connect(String playerId) {
        var player = playerRepository.findById(playerId).orElseThrow();
        var game = gameRepository.findByStatus(WAITING_PLAYER).stream()
                .filter(g -> g.getPlayers().size() < gameConfig.getNumberOfPlayers())
                .min(comparing(Game::getCreatedAt)).orElse(new Game());
        game.getPlayers().add(player);
        if (game.getPlayers().stream().filter(p -> p.getStatus().equals(WAITING)).count() == gameConfig.getNumberOfPlayers())
            game.startGame(getRandomNumber());
        game = gameRepository.save(game);
        if (game.getStatus().equals(RUNNING))
            notifyGameStartedForPlayers(game);
        else
            notifyNewPlayer(game);
    }

    public void executeMove(MoveDTO moveDTO) {
        var game = gameRepository.findById(moveDTO.getGameId()).orElseThrow();
        game.executeMove(moveDTO, gameConfig.getDivisor(), gameConfig.getWinningNumber());
        gameRepository.save(game);
        notifyGameMove(game);
    }

    private void notifyGameStartedForPlayers(Game game) {
        final var starterPlayer = game.getFirstPlayer();
        messagingTemplate.convertAndSend(
                format("%s/%s", starterPlayer.getId(), START_GAME_SUBSCRIBE_PATH),
                "Game started! \n it's your turn");
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(starterPlayer.getId())
                        && player.getStatus().equals(PLAYING))
                .forEach(currentPlayer -> messagingTemplate.convertAndSend(
                        format("%s/%s",starterPlayer.getId(), WAIT_SUBSCRIBE_PATH),
                        format("Game started! \n it's %s turn", starterPlayer.getName())));
    }

    private void notifyNewPlayer(Game game) {
        final var newPlayer  = game.getLastPlayer();
        messagingTemplate.convertAndSend(
                format("%s/%s", WAIT_SUBSCRIBE_PATH, newPlayer.getId()),
                "Waiting for players...");
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(newPlayer.getId())
                        && player.getStatus().equals(WAITING))
                .forEach(currentPlayer -> messagingTemplate.convertAndSend(
                        format("%s/%s", WAIT_SUBSCRIBE_PATH, currentPlayer.getId()),
                        format("Player %s joined", newPlayer.getName())));
    }

    private void notifyGameMove(Game game) {
        switch (game.getStatus()) {
            case RUNNING:
                var nextPlayer = game.getNextPlayer();
                messagingTemplate.convertAndSend(
                        format("%s/%s", ROUND_SUBSCRIBE_PATH, nextPlayer.getId()),
                        "It's your turn!");
                game.getPlayers()
                        .stream()
                        .filter(player -> !player.getId().equals(nextPlayer.getId()) && player.getStatus().equals(PLAYING))
                        .forEach(currentPlayer -> messagingTemplate.convertAndSend(
                                format("%s/%s", WAIT_SUBSCRIBE_PATH, currentPlayer.getId()),
                                format("it's %s turn", nextPlayer.getName())));
                break;
            case FINISHED:
                final var winnerPlayer = game.getWinnerPlayer();
                messagingTemplate.convertAndSend(
                        format("%s/%s", WIN_SUBSCRIBE_PATH, winnerPlayer.getId()),
                        "Congratulations you won the game!");
                game.getPlayers()
                        .stream()
                        .filter(player -> !player.getId().equals(winnerPlayer.getId()) && player.getStatus().equals(FINISHED))
                        .forEach(currentPlayer -> messagingTemplate.convertAndSend(
                                format("%s/%s", LOSE_SUBSCRIBE_PATH, currentPlayer.getId()),
                                format("Player %s won the game!", winnerPlayer.getName())));
                break;
        }
    }

    private Integer getRandomNumber() {
        return new Random().nextInt(gameConfig.getMaxRandomNumber()) + 1;
    }
}
