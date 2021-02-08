package net.feliperocha.gameofthree.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "game-of-three")
@ConstructorBinding
@AllArgsConstructor
@Getter
public class GameConfig {
    private final Integer divisor;
    private final Integer winningNumber;
    private final Integer numberOfPlayers;
    private final Integer maxRandomNumber;
}
