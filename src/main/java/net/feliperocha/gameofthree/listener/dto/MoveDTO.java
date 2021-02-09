package net.feliperocha.gameofthree.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.feliperocha.gameofthree.domain.MoveCommand;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class MoveDTO {
    @NotNull
    private final String gameId;
    @NotNull
    private final String playerId;
    @NotNull
    private final MoveCommand command;
}
