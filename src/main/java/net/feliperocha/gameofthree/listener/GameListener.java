package net.feliperocha.gameofthree.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.listener.dto.StartGameDTO;
import net.feliperocha.gameofthree.service.GameService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@AllArgsConstructor
@Slf4j
public class GameListener {
    private final GameService gameService;

    @MessageMapping("/connect")
    public void connect(@Payload String playerId) { gameService.connect(playerId); }

    @MessageMapping("/start")
    public void move(@RequestBody StartGameDTO startGameDTO) { gameService.start(startGameDTO); }

    @MessageMapping("/move")
    public void move(@RequestBody MoveDTO moveDTO) { gameService.executeMove(moveDTO); }

    @MessageExceptionHandler
    public void exceptionHandler(Exception exception) { log.error(exception.getMessage()); }
}
