package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import net.feliperocha.gameofthree.controller.dto.MoveDTO;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.comparing;
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

    public void startGame(Integer initialNumber) {
        this.players = this.getPlayers().stream().peek(p -> {
            if (p.getStatus().equals(WAITING))
                p.setStatus(PLAYING);
        }).collect(toList());
        this.initialNumber = initialNumber;
        this.status = RUNNING;
    }

    public void executeMove(MoveDTO moveDTO, Integer divisor, Integer winningNumber) {
        var move = this.getMoves().isEmpty() ? new Move(moveDTO.getCommand(), moveDTO.getPlayerId(), this.initialNumber) :
                new Move(moveDTO.getCommand(), moveDTO.getPlayerId(), this.getMoves()
                        .stream()
                        .max(comparing(Move::getCreatedAt))
                        .map(Move::getCurrentNumber).orElse(this.initialNumber));
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

    public Player getPlayer(Long playerId) {
        return this.getPlayers()
                .stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst().orElseThrow();
    }

    public Player getFirstPlayer() {
        return this.getPlayers()
                .stream()
                .filter(player -> !player.getStatus().equals(DISCONNECTED))
                .min(comparing(Player::getCreatedAt))
                .orElseThrow();
    }

    public Player getNextPlayer() {
        final var lastMovePlayer = this.getMoves()
                .stream()
                .max(comparing(Move::getCreatedAt))
                .map(Move::getPlayerId).flatMap(playerId -> this.getPlayers()
                        .stream()
                        .filter(player -> player.getId().equals(playerId))
                        .findFirst()
                ).orElseThrow();
        var nextPlayer = this.getPlayers()
                .stream()
                .filter(currentPlayer -> currentPlayer.getCreatedAt().isAfter(lastMovePlayer.getCreatedAt()) &&
                        currentPlayer.getStatus().equals(PLAYING))
                .min(comparing(Player::getCreatedAt));
        return nextPlayer.orElseGet(() -> this.getPlayers().stream()
                .filter(currentPlayer -> currentPlayer.getStatus().equals(PLAYING))
                .min(comparing(Player::getCreatedAt)).orElseThrow()
        );
    }

    public Player getWinnerPlayer() {
        return this.getPlayers()
                .stream()
                .filter(player -> player.getId().equals(this.winnerPlayerId.orElseThrow()))
                .findFirst().orElseThrow();
    }
}
