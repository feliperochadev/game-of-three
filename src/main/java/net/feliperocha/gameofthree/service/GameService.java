package net.feliperocha.gameofthree.service;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.configuration.GameConfig;
import net.feliperocha.gameofthree.controller.dto.StartGameDTO;
import net.feliperocha.gameofthree.domain.*;
import net.feliperocha.gameofthree.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

import static java.util.Comparator.comparing;
import static net.feliperocha.gameofthree.domain.GameStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.PLAYING;
import static net.feliperocha.gameofthree.domain.PlayerStatus.WAITING;

@Service
@AllArgsConstructor
public class GameService {
    private final GameConfig gameConfig;
    private final GameRepository gameRepository;

    public void startGame(StartGameDTO startGameDTO) {
        var game = gameRepository.findByStatus(WAITING_PLAYER).stream()
                .filter(g -> g.getPlayers().size() < gameConfig.getNumberOfPlayers())
                .min(comparing(Game::getCreatedAt)).orElse(new Game());
        game.getPlayers().add(new Player(startGameDTO.getName(), startGameDTO.getIsPlayingAutomatically()));
        if (game.getPlayers().stream().filter(p -> p.getStatus().equals(WAITING)).count() == gameConfig.getNumberOfPlayers())
            game.startGame();
        game = gameRepository.save(game);
        if (game.getStatus().equals(RUNNING))
            notifyGameStartedForPlayers(game);

    }

    public void executeMove(MoveCommand command, Long gameId, Long playerId) {
        var game = gameRepository.findById(gameId).orElseThrow();
        var move = game.getMoves().isEmpty() ? new Move(command, playerId, getRandomNumber()) :
                new Move(command, playerId, game.getMoves()
                        .stream()
                        .max(comparing(Move::getCreatedAt))
                        .map(Move::getCurrentNumber).orElse(getRandomNumber()));
        game.executeMove(move, gameConfig.getDivisor(), gameConfig.getWinningNumber());
        gameRepository.save(game);
        notifyGameMove(game, playerId);
    }

    private void notifyGameStartedForPlayers(Game game) {
        game.getPlayers()
                .stream()
                .min(comparing(Player::getCreatedAt))
                .ifPresent(starterPlayer -> {
                    //notifyGameStartedForPlayers(game, starterPlayer);
                    game.getPlayers()
                            .stream()
                            .filter(player -> !player.getId().equals(starterPlayer.getId())
                                    && player.getStatus().equals(PLAYING))
                            .forEach(currentPlayer -> System.out.println(currentPlayer));
                });
    }

    private void notifyGameMove(Game game, Long PlayerId) {
        switch (game.getStatus()) {
            case RUNNING:
                break;
            case FINISHED:
                break;
        }
    }

    private Integer getRandomNumber() {
        return new Random().nextInt(gameConfig.getMaxRandomNumber());
    }
}
