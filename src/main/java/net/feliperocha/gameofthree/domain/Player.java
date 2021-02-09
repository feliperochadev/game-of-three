package net.feliperocha.gameofthree.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.feliperocha.gameofthree.domain.PlayerStatus.WAITING;

@Getter
@Setter
public class Player {
    public Player(String name, Boolean isPlayingAutomatically) {
        this.name = name;
        this.isPlayingAutomatically = isPlayingAutomatically;
        this.id = UUID.randomUUID().toString();
        this.status = WAITING;
        this.createdAt = LocalDateTime.now();
    }

    @Id
    private String id;
    private String name;
    private PlayerStatus status;
    private Boolean isPlayingAutomatically;
    private LocalDateTime createdAt;
}
