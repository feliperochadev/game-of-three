package net.feliperocha.gameofthree.listener.dto;

import lombok.Getter;
import net.feliperocha.gameofthree.listener.GameEvent;

@Getter
public class GameMessageDTO {

    public GameMessageDTO(GameEvent type, String gameId) {
        this.type = type;
        this.gameId = gameId;
    }

    public GameMessageDTO(GameEvent type, String gameId, String message) {
        this.type = type;
        this.gameId = gameId;
        this.message = message;
    }

    public GameMessageDTO(GameEvent type, String gameId, String message, Integer currentNumber) {
        this.type = type;
        this.message = message;
        this.gameId = gameId;
        this.currentNumber = currentNumber;
    }

    private final GameEvent type;
    private final String gameId;
    private String message;
    private Integer currentNumber;
}
