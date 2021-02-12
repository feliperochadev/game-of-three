package net.feliperocha.gameofthree.unit;

import net.feliperocha.gameofthree.domain.Move;
import net.feliperocha.gameofthree.domain.MoveCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MoveUnitTest {
    @ParameterizedTest
    @CsvSource({
            "ADD, 8, 3, 3",
            "MAINTAIN, 6, 3, 2",
            "SUBTRACT, 4, 3, 1",
    })
    void get_expected_current_number(MoveCommand command, int previousNumber, int divisor, int expectedNumber) {
        var move = new Move(command, "player-test", previousNumber);
        move.calculateCurrentNumber(divisor);
        Assertions.assertEquals(move.getCurrentNumber(), expectedNumber);
    }
}
