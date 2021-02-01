package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.feliperocha.gameofthree.domain.GameStatus.WAITING_PLAYER;

@Getter
@Setter
public class Game {
    public Game(Player player) {
        this.status = WAITING_PLAYER;
        this.players = List.of(player);
        this.moves = new ArrayList<>();
    }

    @Id
    private Long id;
    private GameStatus status;
    private Optional<Long> winnerPlayerId;
    private List<Move> moves;
    private List<Player> players;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
