package net.feliperocha.gameofthree.listener;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.listener.dto.StartGameDTO;
import net.feliperocha.gameofthree.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@AllArgsConstructor
public class GameListener {

    private final GameService gameService;

    @MessageMapping("/start")
    public void start(StartGameDTO startGameDTO) { gameService.startGame(startGameDTO); }

    @MessageMapping("/move")
    public void start(@RequestBody MoveDTO moveDTO) { gameService.executeMove(moveDTO); }


    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        System.out.println(event);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println(event);
    }
}
