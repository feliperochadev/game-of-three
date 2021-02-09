package net.feliperocha.gameofthree.service;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.configuration.GameConfig;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.listener.dto.StartGameDTO;
import net.feliperocha.gameofthree.domain.*;
import net.feliperocha.gameofthree.repository.GameRepository;
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
    private final SimpMessagingTemplate messagingTemplate;

    private static final String START_GAME_SUBSCRIBE_PATH = "/queue/subscribe/start";
    private static final String WAIT_SUBSCRIBE_PATH = "/queue/subscribe/wait";
    private static final String ROUND_SUBSCRIBE_PATH = "/queue/subscribe/round";
    private static final String WIN_SUBSCRIBE_PATH = "//queuesubscribe/win";
    private static final String LOSE_SUBSCRIBE_PATH = "/queue/subscribe/lose";

    public void startGame(StartGameDTO startGameDTO) {
        var game = gameRepository.findByStatus(WAITING_PLAYER).stream()
                .filter(g -> g.getPlayers().size() < gameConfig.getNumberOfPlayers())
                .min(comparing(Game::getCreatedAt)).orElse(new Game());
        game.getPlayers().add(new Player(startGameDTO.getName(), startGameDTO.getIsPlayingAutomatically()));
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
        messagingTemplate.convertAndSendToUser(starterPlayer.getId().toString(),
                START_GAME_SUBSCRIBE_PATH + game.getId(), "Game started! \n it's your turn");
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(starterPlayer.getId())
                        && player.getStatus().equals(PLAYING))
                .forEach(currentPlayer -> messagingTemplate.convertAndSendToUser(currentPlayer.getId().toString(),
                        WAIT_SUBSCRIBE_PATH + game.getId(),
                        format("Game started! \n it's %s turn", starterPlayer.getName())));
    }

    private void notifyNewPlayer(Game game) {
        final var newPlayer  = game.getLastPlayer();
        messagingTemplate.convertAndSend(
                WAIT_SUBSCRIBE_PATH, "Waiting for players...");
        game.getPlayers()
                .stream()
                .filter(player -> !player.getId().equals(newPlayer.getId())
                        && player.getStatus().equals(WAITING))
                .forEach(currentPlayer -> messagingTemplate.convertAndSend(WAIT_SUBSCRIBE_PATH,
                        format("Player %s joined", newPlayer.getName())));
    }

    private void notifyGameMove(Game game) {
        switch (game.getStatus()) {
            case RUNNING:
                var nextPlayer = game.getNextPlayer();
                messagingTemplate.convertAndSendToUser(nextPlayer.getId().toString(),
                        ROUND_SUBSCRIBE_PATH + game.getId(), "It's your turn!");
                game.getPlayers()
                        .stream()
                        .filter(player -> !player.getId().equals(nextPlayer.getId()) && player.getStatus().equals(PLAYING))
                        .forEach(currentPlayer -> messagingTemplate.convertAndSendToUser(currentPlayer.getId().toString(),
                                WAIT_SUBSCRIBE_PATH + game.getId(),
                                format("it's %s turn", nextPlayer.getName())));
                break;
            case FINISHED:
                final var winnerPlayer = game.getWinnerPlayer();
                messagingTemplate.convertAndSendToUser(winnerPlayer.getId().toString(),
                        WIN_SUBSCRIBE_PATH + game.getId(), "Congratulations you won the game!");
                game.getPlayers()
                        .stream()
                        .filter(player -> !player.getId().equals(winnerPlayer.getId()) && player.getStatus().equals(FINISHED))
                        .forEach(currentPlayer -> messagingTemplate.convertAndSendToUser(currentPlayer.getId().toString(),
                                LOSE_SUBSCRIBE_PATH + game.getId(),
                                format("Player %s won the game!", winnerPlayer.getName())));
                break;
        }
    }

    private Integer getRandomNumber() {
        return new Random().nextInt(gameConfig.getMaxRandomNumber()) + 1;
    }
}
