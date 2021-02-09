package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Move;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoveRepository extends MongoRepository<Move, String> {
}
