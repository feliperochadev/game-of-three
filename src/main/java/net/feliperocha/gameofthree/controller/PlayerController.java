package net.feliperocha.gameofthree.controller;

import lombok.AllArgsConstructor;
import net.feliperocha.gameofthree.controller.dto.CreatePlayerDTO;
import net.feliperocha.gameofthree.domain.Player;
import net.feliperocha.gameofthree.repository.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
public class PlayerController {
    private final PlayerRepository playerRepository;

    @PostMapping("/player")
    public ResponseEntity<Player> createPlayer(@RequestBody @Valid CreatePlayerDTO createPlayerDTO) {
        var player = new Player(createPlayerDTO.getName(), createPlayerDTO.getIsPlayingAutomatically());
        playerRepository.save(player);
        return status(CREATED).body(player);
    }
}
