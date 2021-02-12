package net.feliperocha.gameofthree.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class StartGameDTO {
    @NotNull
    private final Integer initialNumber;
    @NotNull
    private final String playerId;
    @NotNull
    private final String gameId;
}
