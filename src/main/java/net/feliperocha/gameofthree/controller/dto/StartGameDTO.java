package net.feliperocha.gameofthree.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class StartGameDTO {
    @NotBlank
    private final String name;
    @NotNull
    private final Boolean isPlayingAutomatically;
}
