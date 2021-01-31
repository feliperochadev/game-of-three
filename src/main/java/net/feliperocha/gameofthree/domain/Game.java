package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static net.feliperocha.gameofthree.domain.GameStatus.STARTED;

@Getter
@Setter
public class Game {
    @Id
    private Long id;
    private GameStatus status = STARTED;
    private Optional<Long> winnerPlayerId;
    private List<Move> moves;
    private List<Player> players;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
