package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, Long> {
}
