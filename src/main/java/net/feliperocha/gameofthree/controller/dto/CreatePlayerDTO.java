package net.feliperocha.gameofthree.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class CreatePlayerDTO {
    @NotBlank
    private final String name;
    @NotNull
    private final Boolean isPlayingAutomatically;
}
