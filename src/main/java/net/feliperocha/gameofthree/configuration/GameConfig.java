package net.feliperocha.gameofthree.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "game-of-three")
@AllArgsConstructor
@Getter
public class GameConfig {
    private final Long divisor;
    private final Long winningNumber;
    private final Long numberOfPlayers;
}
