package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Move;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MoveRepository extends MongoRepository<Move, Long> {
}
