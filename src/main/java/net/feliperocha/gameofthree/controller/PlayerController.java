package net.feliperocha.gameofthree.controller;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.controller.dto.CreatePlayerDTO;
import net.feliperocha.gameofthree.domain.Player;
import net.feliperocha.gameofthree.repository.PlayerRepository;
import net.feliperocha.gameofthree.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
public class PlayerController {
    private final PlayerRepository playerRepository;
    private final GameService gameService;

    @PostMapping("/player")
    public ResponseEntity<Player> createPlayer(@RequestBody @Valid CreatePlayerDTO createPlayerDTO) {
        var player = new Player(createPlayerDTO.getName(), createPlayerDTO.getIsPlayingAutomatically());
        playerRepository.save(player);
        return status(CREATED).body(player);
    }

    @DeleteMapping("/player/{id}/disconnect")
    public void disconnect(@PathVariable String id, @RequestParam(value = "gameId") String gameId) {
        gameService.disconnectPlayer(id, gameId);
    }
}
