package net.feliperocha.gameofthree.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class StartGameDTO {
    @NotBlank
    private final String name;
    @NotNull
    private final Boolean isPlayingAutomatically;
}
