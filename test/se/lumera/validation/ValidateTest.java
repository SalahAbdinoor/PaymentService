package se.lumera.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateTest {

    Validate validate = new Validate();

    /**
     * throws NumberFormatException if:
     * - contains letter
     * - lacks space between clearing number account number
     */
    @Test
    void invalidAccountNumber() {

        var invalidInput = new Object() {

            // invalid: Has letter in account number
            String containsLetter = "5555 555X555555";

            // invalid: lacks space between clearing number account number
            final String lacksSpace = "55555555555555";
        };

        // Exception thrown when: containing letter
        Exception containsLetterException = assertThrows(NumberFormatException.class, () -> {
            validate.validAccountNumber(invalidInput.containsLetter);
        });

        // Exception thrown when: lacks space
        Exception lacksSpaceException = assertThrows(NumberFormatException.class, () -> {
            validate.validAccountNumber(invalidInput.lacksSpace);
        });

        // method removes space before converting to number
        invalidInput.containsLetter = "5555555X555555";

        // ----------------- contains letter: error message
        String containsLetterExpectedMessage = "Invalid account number: " + invalidInput.containsLetter;
        String containsLetterActualMessage = containsLetterException.getMessage();

        // ----------------- lacks space: error message
        String lacksSpaceExpectedMessage = "Clearing number and account number is not seperated with ' ' (space).";
        String lacksSpaceActualMessage = lacksSpaceException.getMessage();

        // ----------------- Assertions
        assertTrue(lacksSpaceActualMessage.contains(lacksSpaceExpectedMessage));
        assertTrue(containsLetterActualMessage.contains(containsLetterExpectedMessage));
    }

}