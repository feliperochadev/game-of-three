package net.feliperocha.gameofthree.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.feliperocha.gameofthree.domain.MoveCommand;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class MoveDTO {
    @NotNull
    private final Long gameId;
    @NotNull
    private final Long playerId;
    @NotNull
    private final MoveCommand command;
}
