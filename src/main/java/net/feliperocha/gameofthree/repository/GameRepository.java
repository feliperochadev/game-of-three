package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Game;
import net.feliperocha.gameofthree.domain.GameStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    List<Game> findByStatus(GameStatus status);
}
