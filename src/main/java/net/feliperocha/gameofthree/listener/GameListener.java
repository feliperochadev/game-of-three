package net.feliperocha.gameofthree.listener;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import net.feliperocha.gameofthree.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@AllArgsConstructor
public class GameListener {

    private final GameService gameService;

    @MessageMapping("/connect")
    public void connect(@Payload String playerId) { gameService.connect(playerId); }

    @MessageMapping("/move")
    public void move(@RequestBody MoveDTO moveDTO) { gameService.executeMove(moveDTO); }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println(event);
    }
}
