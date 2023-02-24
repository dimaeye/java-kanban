package domain;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private Epic epic;

    private final EasyRandom generator = new EasyRandom();

    @BeforeEach
    void beforeEach() {
        epic = new Epic(generator.nextInt(), generator.nextObject(String.class), generator.nextObject(String.class));
    }

    @Test
    void shouldReturnNewStatusOfEpicWhichContainsEmptyListOfSubtasks() {
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldReturnNewStatusOfEpicWhichContainsListOfSubtasksOnlyWithNewStatus() {
        final int subtasksCount = 10;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.NEW);
            epic.addRelatedTask(subtask);
        }

        assertAll(
                () -> assertEquals(subtasksCount, epic.getAllRelatedTasks().size()),
                () -> assertEquals(TaskStatus.NEW, epic.getStatus())
        );
    }

    @Test
    void shouldReturnDoneStatusOfEpicWhichContainsListOfSubtasksOnlyWithDoneStatus() {
        final int subtasksCount = 10;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.DONE);
            epic.addRelatedTask(subtask);
        }

        assertAll(
                () -> assertEquals(subtasksCount, epic.getAllRelatedTasks().size()),
                () -> assertEquals(TaskStatus.DONE, epic.getStatus())
        );
    }

    @Test
    void shouldReturnInProgressStatusOfEpicWhichContainsListOfSubtasksWithNewOrDoneStatus() {
        final int subtasksCount = 10;
        for (int i = 0; i < subtasksCount; i++) {
            Subtask subtask;
            if (i % 2 == 0)
                subtask = generateSubtaskWithStatus(TaskStatus.NEW);
            else
                subtask = generateSubtaskWithStatus(TaskStatus.DONE);
            epic.addRelatedTask(subtask);
        }

        assertAll(
                () -> assertEquals(subtasksCount, epic.getAllRelatedTasks().size()),
                () -> assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus())
        );
    }

    @Test
    void shouldReturnInProgressStatusOfEpicWhichContainsListOfSubtasksOnlyWithInProgressStatus() {
        final int subtasksCount = 10;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.IN_PROGRESS);
            epic.addRelatedTask(subtask);
        }

        assertAll(
                () -> assertEquals(subtasksCount, epic.getAllRelatedTasks().size()),
                () -> assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus())
        );
    }

    @Test
    void shouldReturnDurationOfEpicEqualToSumDurationOfAllSubtasks() {
        final int subtasksCount = 10;
        int sumDurationOfAllSubtasks = 0;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.IN_PROGRESS);
            subtask.setStartTime(LocalDateTime.now().minusHours(i));
            final int duration = (i + 1) * 4;
            sumDurationOfAllSubtasks += duration;
            subtask.setDuration(duration);
            epic.addRelatedTask(subtask);
        }

        final int expectedEpicDuration = sumDurationOfAllSubtasks;

        assertEquals(expectedEpicDuration, epic.getDuration());
    }

    @Test
    void shouldReturnStartTimeOfEpicEqualToStartTimeOfEarliestSubtask() {
        final int subtasksCount = 10;
        LocalDateTime startTimeOfEarliestSubtask = null;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.IN_PROGRESS);
            subtask.setStartTime(LocalDateTime.now().minusHours(i));
            subtask.setDuration(15);
            if (i == subtasksCount - 1)
                startTimeOfEarliestSubtask = subtask.getStartTime();
            epic.addRelatedTask(subtask);
        }

        final LocalDateTime expectedEpicStartTime = startTimeOfEarliestSubtask;

        assertAll(
                () -> assertNotNull(epic.getStartTime()),
                () -> assertEquals(expectedEpicStartTime, epic.getStartTime())
        );
    }

    @Test
    void shouldReturnEndTimeOfEpicEqualToEndTimeOfLatestSubtask() {
        final int subtasksCount = 10;
        LocalDateTime endTimeOfLatestSubtask = null;
        for (int i = 0; i < subtasksCount; i++) {
            final Subtask subtask = generateSubtaskWithStatus(TaskStatus.IN_PROGRESS);
            subtask.setStartTime(LocalDateTime.now().minusHours(i));
            subtask.setDuration(5);
            if (i == 0) {
                subtask.setDuration(Byte.MAX_VALUE);
                endTimeOfLatestSubtask = subtask.getEndTime();
            }
            epic.addRelatedTask(subtask);
        }

        final LocalDateTime expectedEpicEndTime = endTimeOfLatestSubtask;

        assertAll(
                () -> assertNotNull(epic.getStartTime()),
                () -> assertEquals(expectedEpicEndTime, epic.getEndTime())
        );
    }

    private Subtask generateSubtaskWithStatus(TaskStatus taskStatus) {
        final Subtask subtask = new Subtask(
                generator.nextInt(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        subtask.setStatus(taskStatus);
        return subtask;
    }
}