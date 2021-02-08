package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
public class Move {
    public Move(MoveCommand command, Long playerId, Integer previousNumber) {
        this.command = command;
        this.playerId = playerId;
        this.previousNumber = previousNumber;
    }

    @Id
    private Long id;
    private Long playerId;
    private MoveCommand command;
    private Integer previousNumber;
    private Integer currentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void calculateCurrentNumber(Integer divisor) {
        switch (this.command) {
            case ADDITION:
                this.currentNumber = (this.previousNumber + 1) / divisor;
                break;
            case MAINTAIN:
                this.currentNumber = this.previousNumber / divisor;
                break;
            case SUBTRACTION:
                this.currentNumber = (this.previousNumber - 1) / divisor;
                break;
        }
    }
}
