package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, Long> {
}
