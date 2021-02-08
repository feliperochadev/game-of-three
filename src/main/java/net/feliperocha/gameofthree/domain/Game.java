package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.stream.Collectors.toList;
import static net.feliperocha.gameofthree.domain.GameStatus.RUNNING;
import static net.feliperocha.gameofthree.domain.GameStatus.WAITING_PLAYER;
import static net.feliperocha.gameofthree.domain.PlayerStatus.*;

@Getter
@Setter
public class Game {
    public Game() {
        this.status = WAITING_PLAYER;
        this.players = new ArrayList<>();
        this.moves = new ArrayList<>();
    }

    @Id
    private Long id;
    private GameStatus status;
    private Optional<Long> winnerPlayerId;
    private Integer initialNumber;
    private List<Move> moves;
    private List<Player> players;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void startGame() {
        this.players = this.getPlayers().stream().peek(p -> {
            if (p.getStatus().equals(WAITING))
                p.setStatus(PLAYING);
        }).collect(toList());
        this.initialNumber = new Random().nextInt(100);
        this.status = RUNNING;
    }

    public void executeMove(Move move, Integer divisor, Integer winningNumber) {
        move.calculateCurrentNumber(divisor);
        this.moves.add(move);
        if (move.getCurrentNumber().equals(winningNumber)) {
            this.winnerPlayerId = Optional.of(move.getPlayerId());
            this.setStatus(GameStatus.FINISHED);
            this.getPlayers()
                    .stream()
                    .filter(currentPlayer -> currentPlayer.getStatus().equals(PLAYING))
                    .forEach(currentPlayer -> currentPlayer.setStatus(FINISHED));
        }
    }
}
