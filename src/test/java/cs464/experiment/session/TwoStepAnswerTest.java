package cs464.experiment.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwoStepAnswerTest {

    @Test
    void aiIsHiddenUntilFirstSubmit() {
        TwoStepAnswer a = new TwoStepAnswer();
        assertFalse(a.aiVisible());
        assertFalse(a.canSubmit());
        a.select(2);
        assertFalse(a.aiVisible());
        a.submit();
        assertTrue(a.aiVisible());
    }

    @Test
    void cannotSubmitWithoutSelection() {
        assertThrows(IllegalStateException.class, () -> new TwoStepAnswer().submit());
    }

    @Test
    void notCompleteUntilSecondSubmit() {
        TwoStepAnswer a = new TwoStepAnswer();
        a.select(1);
        a.submit();
        assertFalse(a.isComplete());
        a.submit();
        assertTrue(a.isComplete());
    }

    @Test
    void firstSubmitRecordsOriginalAndSecondRecordsFinal() {
        TwoStepAnswer a = new TwoStepAnswer();
        a.select(1);
        a.select(2);
        a.submit();
        assertEquals(2, a.originalIndex());
        a.select(0);
        assertEquals(2, a.originalIndex());
        a.submit();
        assertEquals(2, a.originalIndex());
        assertEquals(0, a.finalIndex());
    }

    @Test
    void keepingTheOriginalAnswerRecordsItAsFinal() {
        TwoStepAnswer a = new TwoStepAnswer();
        a.select(3);
        a.submit();
        a.submit();
        assertEquals(3, a.originalIndex());
        assertEquals(3, a.finalIndex());
    }

    @Test
    void noChangesAfterCompletion() {
        TwoStepAnswer a = new TwoStepAnswer();
        a.select(1);
        a.submit();
        a.submit();
        assertThrows(IllegalStateException.class, a::submit);
        assertThrows(IllegalStateException.class, () -> a.select(0));
    }
}
