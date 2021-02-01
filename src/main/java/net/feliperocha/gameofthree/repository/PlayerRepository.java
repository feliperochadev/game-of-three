package net.feliperocha.gameofthree.repository;

import net.feliperocha.gameofthree.domain.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<Player, Long> {
}
