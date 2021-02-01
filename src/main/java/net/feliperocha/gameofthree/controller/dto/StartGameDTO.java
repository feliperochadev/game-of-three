package net.feliperocha.gameofthree.controller.dto;

import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Service
public class StartGameDTO {
    @NotBlank
    private String name;
    @NotNull
    private Boolean isPlayingAutomatically;
}
