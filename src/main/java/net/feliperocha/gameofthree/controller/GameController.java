package net.feliperocha.gameofthree.controller;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.controller.dto.StartGameDTO;
import net.feliperocha.gameofthree.domain.MoveCommand;
import net.feliperocha.gameofthree.service.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/start")
    public void start(@Valid @RequestBody StartGameDTO startGameDTO) {
        gameService.startGame(startGameDTO);
    }

    @MessageMapping("{gameId}/move/{playerId}")
    public void start(@PathVariable Long gameId, @PathVariable Long playerId, @RequestBody MoveCommand command) {
        gameService.executeMove(command, gameId, playerId);
    }
}
