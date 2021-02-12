package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static net.feliperocha.gameofthree.domain.GameStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.FINISHED;

@Getter
@Setter
public class Game {
    public Game(Integer initialNumber) {
        this.initialNumber = initialNumber;
        this.status = WAITING_PLAYERS;
        this.winnerPlayerId = empty();
        this.players = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    @Id
    private String id;
    private GameStatus status;
    private Optional<String> winnerPlayerId;
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
        this.status = RUNNING;
    }

    public Move executeMove(MoveDTO moveDTO, Integer divisor, Integer winningNumber) {
        var move = this.getMoves().isEmpty() ?
                new Move(moveDTO.getCommand(), moveDTO.getPlayerId(), this.initialNumber) :
                new Move(moveDTO.getCommand(), moveDTO.getPlayerId(), this.getMoves()
                        .stream()
                        .max(comparing(Move::getCreatedAt))
                        .map(Move::getCurrentNumber).orElse(this.initialNumber));
        move.calculateCurrentNumber(divisor);
        this.moves.add(move);
        if (move.getCurrentNumber() <= winningNumber) {
            if (move.getCurrentNumber().equals(winningNumber))
                this.winnerPlayerId = Optional.of(move.getPlayerId());
            this.setStatus(GameStatus.FINISHED);
            this.getPlayers()
                    .stream()
                    .filter(currentPlayer -> currentPlayer.getStatus().equals(PLAYING))
                    .forEach(currentPlayer -> currentPlayer.setStatus(FINISHED));
        }
        return move;
    }

    public Player getFirstPlayer() {
        return this.getPlayers()
                .stream()
                .filter(player -> !player.getStatus().equals(PlayerStatus.DISCONNECTED))
                .min(comparing(Player::getCreatedAt))
                .orElseThrow();
    }

    public Player getLastPlayer() {
        return this.getPlayers()
                .stream()
                .filter(player -> !player.getStatus().equals(PlayerStatus.DISCONNECTED))
                .max(comparing(Player::getCreatedAt))
                .orElseThrow();
    }

    public Player getNextPlayer() {
        var lastMovePlayer = this.getMoves()
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

    public void disconnectGame() {
        this.setStatus(GameStatus.DISCONNECTED);
        this.getPlayers().forEach(currentPlayer -> currentPlayer.setStatus(PlayerStatus.DISCONNECTED));
    }

    public Optional<Player> getWinnerPlayer() {
        if (this.winnerPlayerId.isEmpty())
            return empty();
        return this.getPlayers().stream().filter(player -> player.getId().equals(this.winnerPlayerId.get())).findFirst();
    }
}
