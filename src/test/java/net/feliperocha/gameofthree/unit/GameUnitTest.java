package net.feliperocha.gameofthree.unit;

import net.feliperocha.gameofthree.domain.*;
import net.feliperocha.gameofthree.listener.dto.MoveDTO;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static net.feliperocha.gameofthree.domain.GameStatus.*;
import static net.feliperocha.gameofthree.domain.MoveCommand.*;
import static net.feliperocha.gameofthree.domain.PlayerStatus.PLAYING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameUnitTest {
    private final static Integer DIVISOR = 3;
    private final static Integer WINNING_NUMBER = 1;
    private final static Integer INITIAL_NUMBER = 56;
    private final static Player FIRST_PLAYER = new Player("First", true);
    private final static Player SECOND_PLAYER = new Player("Second", false);;
    private Game GAME;

    @BeforeAll
    void create_test_game() {
        GAME = new Game();
        GAME.setInitialNumber(INITIAL_NUMBER);
        GAME.getPlayers().addAll(List.of(FIRST_PLAYER, SECOND_PLAYER));
    }

    @Test
    void execute_moves_until_game_end() {
        //given
        FIRST_PLAYER.setStatus(PLAYING);
        SECOND_PLAYER.setStatus(PLAYING);

        //when
        GAME.executeMove(new MoveDTO(GAME.getId(), SECOND_PLAYER.getId(), ADD), DIVISOR, WINNING_NUMBER);
        GAME.executeMove(new MoveDTO(GAME.getId(), FIRST_PLAYER.getId(), SUBTRACT), DIVISOR, WINNING_NUMBER);
        GAME.executeMove(new MoveDTO(GAME.getId(), SECOND_PLAYER.getId(), MAINTAIN), DIVISOR, WINNING_NUMBER);
        GAME.executeMove(new MoveDTO(GAME.getId(), FIRST_PLAYER.getId(), ADD), DIVISOR, WINNING_NUMBER);

        //then
        assertTrue(GAME.getPlayers().stream().allMatch(p -> p.getStatus().equals(PlayerStatus.FINISHED)));
        assertEquals(GAME.getStatus(), FINISHED);
        assertEquals(GAME.getMoves().stream().max(comparing(Move::getCreatedAt)).get().getCurrentNumber(), WINNING_NUMBER);
        assertEquals(GAME.getWinnerPlayer(), Optional.of(FIRST_PLAYER));
    }

    @Test
    void start_game() {
        GAME.startGame();
        assertEquals(GAME.getStatus(), RUNNING);
        assertTrue(GAME.getPlayers().stream().allMatch(p -> p.getStatus().equals(PLAYING)));
    }

    @Test
    void get_first_player() {
        assertEquals(GAME.getFirstPlayer(), FIRST_PLAYER);
    }

    @Test
    void get_last_player() {
        assertEquals(GAME.getLastPlayer(), SECOND_PLAYER);
    }

    @Test
    void get_next_player() {
        //when
        FIRST_PLAYER.setStatus(PLAYING);
        SECOND_PLAYER.setStatus(PLAYING);

        //then
        assertEquals(GAME.getNextPlayer(FIRST_PLAYER), SECOND_PLAYER);
        assertEquals(GAME.getNextPlayer(SECOND_PLAYER), FIRST_PLAYER);
    }

    @Test
    void disconnect_game() {
        GAME.disconnectGame();
        assertEquals(GAME.getStatus(), DISCONNECTED);
    }

    @Test
    void get_winner_player() {
        assertEquals(GAME.getWinnerPlayer(), Optional.empty());
        GAME.setWinnerPlayerId(Optional.of(FIRST_PLAYER.getId()));
        assertEquals(GAME.getWinnerPlayer(), Optional.of(FIRST_PLAYER));
    }
}
