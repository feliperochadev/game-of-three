package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class Move {
    public Move(MoveCommand command, Long previousNumber) {
        this.command = command;
        this.previousNumber = previousNumber;
    }

    @Id
    private Long id;
    private MoveCommand command;
    private Long previousNumber;
    private Optional<Long> currentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
