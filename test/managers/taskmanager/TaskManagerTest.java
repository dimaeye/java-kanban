package managers.taskmanager;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskStatus;
import domain.exceptions.CreateTaskException;
import domain.exceptions.OverlappingTaskTimeException;
import domain.exceptions.TaskNotFoundException;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected final EasyRandom generator = new EasyRandom();

    abstract protected void beforeEach();

    abstract protected void afterEach();

    @Test
    protected void getAllTasks() {
        final int tasksCount = 10;
        List<Task> tasks = new ArrayList<>(tasksCount);

        for (int i = 0; i < tasksCount; i++) {
            Task task = new Task(
                    taskManager.getUniqueTaskId(),
                    generator.nextObject(String.class),
                    generator.nextObject(String.class)
            );
            tasks.add(task);
            taskManager.createTask(task);
        }

        assertEquals(tasks, taskManager.getAllTasks());
    }

    @Test
    protected void shouldReturnEmptyListOfTasksWhenTasksNotCreated() {
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    protected void shouldReturnEmptyListOfTasksAfterRemoveAllTasks() {
        final int tasksCount = 10;
        generator.objects(Task.class, tasksCount).forEach(task -> {
            task.setStartTime(null);
            taskManager.createTask(task);
        });
        assertAll(
                () -> assertEquals(tasksCount, taskManager.getAllTasks().size()),
                () -> {
                    taskManager.removeAllTasks();
                    assertTrue(taskManager.getAllTasks().isEmpty());
                }
        );
    }

    @Test
    protected void shouldReturnTaskByIdWhenTaskCreated() {
        final Task expectedTask = generator.nextObject(Task.class);
        taskManager.createTask(expectedTask);

        final Task actualTask = taskManager.getTask(expectedTask.getId());

        assertEquals(expectedTask, actualTask);
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenGetTaskByBadId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final int randomTaskId = task.getId() + generator.nextInt();
        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getTask(randomTaskId));

        assertEquals(getMessageTaskNotFoundException(randomTaskId), taskNotFoundException.getMessage());
    }

    @Test
    protected void shouldThrowCreateTaskExceptionWhenTaskAlreadyExistWithSameId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final Task newTask =
                new Task(task.getId(), generator.nextObject(String.class), generator.nextObject(String.class));

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createTask(newTask));

        assertEquals(getMessageCreateTaskException(task.getId()), createTaskException.getMessage());
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenCreateTasksWithSameTimeAndZeroDuration() {
        final Task firstTask = generator.nextObject(Task.class);
        firstTask.setStartTime(LocalDateTime.now());
        firstTask.setDuration(0);

        final Task secondTask = generator.nextObject(Task.class);
        secondTask.setStartTime(LocalDateTime.now());
        secondTask.setDuration(0);

        taskManager.createTask(firstTask);

        OverlappingTaskTimeException overlappingTaskTimeException = assertThrows(
                OverlappingTaskTimeException.class, () -> taskManager.createTask(secondTask)
        );
        assertEquals(
                getMessageOverlappingTaskTimeException(secondTask.getId()),
                overlappingTaskTimeException.getMessage()
        );
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenStart2BeforeStart1AndEnd2BeforeEnd1AndAfterStart1() {
        //startTime2<startTime1<endTime2<endTime1
        final LocalDateTime startTime1 = LocalDateTime.now();
        final LocalDateTime startTime2 = LocalDateTime.now().minusHours(2);

        final LocalDateTime endTime1 = LocalDateTime.now().plusHours(2);
        final LocalDateTime endTime2 = LocalDateTime.now().plusHours(1);

        assertMessageOfOverlappingTaskTimeException(startTime1, endTime1, startTime2, endTime2);
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenStart2BeforeStart1AndStart1BeforeEnd1AndEnd1BeforeEnd2() {
        //startTime2<startTime1<endTime1<endTime2
        final LocalDateTime startTime1 = LocalDateTime.now();
        final LocalDateTime startTime2 = LocalDateTime.now().minusHours(2);

        final LocalDateTime endTime1 = LocalDateTime.now().plusHours(1);
        final LocalDateTime endTime2 = LocalDateTime.now().plusHours(2);

        assertMessageOfOverlappingTaskTimeException(startTime1, endTime1, startTime2, endTime2);
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenStart1BeforeStart2AndStart2BeforeEnd1AndEnd1BeforeEnd2() {
        //startTime1<startTime2<endTime1<endTime2
        final LocalDateTime startTime1 = LocalDateTime.now().minusHours(2);
        final LocalDateTime startTime2 = LocalDateTime.now();

        final LocalDateTime endTime1 = LocalDateTime.now().plusHours(1);
        final LocalDateTime endTime2 = LocalDateTime.now().plusHours(2);

        assertMessageOfOverlappingTaskTimeException(startTime1, endTime1, startTime2, endTime2);
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenStart1BeforeStart2AndStart2BeforeEnd2AndEnd2BeforeEnd1() {
        //startTime1<startTime2<endTime2<endTime1
        final LocalDateTime startTime1 = LocalDateTime.now().minusHours(2);
        final LocalDateTime startTime2 = LocalDateTime.now();

        final LocalDateTime endTime1 = LocalDateTime.now().plusHours(2);
        final LocalDateTime endTime2 = LocalDateTime.now().plusHours(1);

        assertMessageOfOverlappingTaskTimeException(startTime1, endTime1, startTime2, endTime2);
    }

    private void assertMessageOfOverlappingTaskTimeException(
            LocalDateTime startTime1, LocalDateTime endTime1, LocalDateTime startTime2, LocalDateTime endTime2
    ) {
        final Task firstTask = generator.nextObject(Task.class);
        firstTask.setStartTime(startTime1);
        firstTask.setDuration((int) Duration.between(startTime1, endTime1).toMinutes());

        final Task secondTask = generator.nextObject(Task.class);
        secondTask.setStartTime(startTime2);
        secondTask.setDuration((int) Duration.between(startTime2, endTime2).toMinutes());

        taskManager.createTask(firstTask);

        OverlappingTaskTimeException overlappingTaskTimeException = assertThrows(
                OverlappingTaskTimeException.class, () -> taskManager.createTask(secondTask)
        );

        assertEquals(
                getMessageOverlappingTaskTimeException(secondTask.getId()), overlappingTaskTimeException.getMessage()
        );
    }

    @Test
    protected void updateTask() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);
        final int taskId = task.getId();

        final String newTaskDescription = generator.nextObject(String.class);
        final TaskStatus taskStatus = TaskStatus.IN_PROGRESS;

        final Task savedTask = taskManager.getTask(taskId);
        savedTask.setDescription(newTaskDescription);
        savedTask.setStatus(taskStatus);

        taskManager.updateTask(savedTask);
        assertEquals(savedTask, taskManager.getTask(taskId));
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedTask() {
        final Task task = generator.nextObject(Task.class);

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateTask(task));

        assertEquals(getMessageTaskNotFoundException(task.getId()), taskNotFoundException.getMessage());
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenUpdateTaskWithOverlappingTime() {
        final Task firstTask = generator.nextObject(Task.class);
        firstTask.setStartTime(LocalDateTime.now());
        firstTask.setDuration(60);

        final Task secondTask = generator.nextObject(Task.class);
        secondTask.setStartTime(LocalDateTime.now().minusHours(2));
        secondTask.setDuration(15);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);

        final Task taskForUpdate = taskManager.getTask(secondTask.getId());
        taskForUpdate.setStartTime(LocalDateTime.now());

        OverlappingTaskTimeException overlappingTaskTimeException = assertThrows(OverlappingTaskTimeException.class,
                () -> taskManager.updateTask(taskForUpdate)
        );

        assertEquals(
                getMessageOverlappingTaskTimeException(taskForUpdate.getId()),
                overlappingTaskTimeException.getMessage()
        );
    }

    @Test
    protected void removeTask() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        taskManager.removeTask(task.getId());

        Optional<Task> optionalTask =
                taskManager.getAllTasks().stream().filter(t -> t.getId() == task.getId()).findFirst();

        assertTrue(optionalTask.isEmpty());
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenRemoveTaskByRandomId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final int randomTaskId = task.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(
                TaskNotFoundException.class,
                () -> taskManager.removeTask(randomTaskId)
        );

        assertEquals(getMessageTaskNotFoundException(randomTaskId), taskNotFoundException.getMessage());
    }

    @Test
    protected void getUniqueTaskId() {
        assertEquals(1, taskManager.getUniqueTaskId());
    }

    @Test
    protected void getAllEpics() {
        final int epicsCount = 10;
        final List<Epic> epics = new ArrayList<>(epicsCount);

        for (int i = 0; i < 10; i++) {
            Epic epic = new Epic(
                    taskManager.getUniqueEpicId(),
                    generator.nextObject(String.class),
                    generator.nextObject(String.class)
            );
            epics.add(epic);
            taskManager.createEpic(epic);
        }

        assertEquals(epics, taskManager.getAllEpics());
    }

    @Test
    protected void shouldReturnEmptyListOfEpicsWhenEpicsNotCreated() {
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    protected void shouldReturnEmptyListOfEpicsAndSubtasksAfterRemoveAllEpics() {
        final int subtasksCount = 10;
        final Epic epic = new Epic(
                generator.nextInt(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);
        for (int i = 0; i < subtasksCount; i++) {
            taskManager.createSubtask(
                    new Subtask(
                            generator.nextInt(), generator.nextObject(String.class), generator.nextObject(String.class),
                            epic
                    )
            );
        }

        taskManager.removeAllEpics();

        assertAll(
                () -> assertTrue(taskManager.getAllEpics().isEmpty()),
                () -> assertTrue(taskManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    protected void getEpic() {
        final Epic expectedEpic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(expectedEpic);

        final Epic actualEpic = taskManager.getEpic(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenGetEpicByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int randomEpicId = epic.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getEpic(randomEpicId)
        );

        assertEquals(getMessageTaskNotFoundException(randomEpicId), taskNotFoundException.getMessage());
    }


    @Test
    protected void createEpic() {
        final Epic expectedEpic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(expectedEpic);

        final Epic actualEpic = taskManager.getEpic(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    protected void shouldThrowCreateTaskExceptionWhenEpicAlreadyExistWithSameId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Epic newEpic = new Epic(
                epic.getId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createEpic(newEpic)
        );

        assertEquals(getMessageCreateTaskException(newEpic.getId()), createTaskException.getMessage());
    }

    @Test
    protected void updateEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);
        final int epicId = epic.getId();

        final String newEpicDescription = generator.nextObject(String.class);
        final String newEpicTitle = generator.nextObject(String.class);

        final Epic savedEpic = taskManager.getEpic(epicId);
        savedEpic.setDescription(newEpicDescription);
        savedEpic.setTitle(newEpicTitle);

        taskManager.updateEpic(savedEpic);
        assertEquals(savedEpic, taskManager.getEpic(epicId));
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateEpic(epic));

        assertEquals(getMessageTaskNotFoundException(epic.getId()), taskNotFoundException.getMessage());
    }

    @Test
    protected void removeEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        taskManager.removeEpic(epic.getId());

        Optional<Epic> optionalTask =
                taskManager.getAllEpics().stream().filter(t -> t.getId() == epic.getId()).findFirst();

        assertTrue(optionalTask.isEmpty());
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenRemoveEpicByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int randomEpicId = epic.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(
                TaskNotFoundException.class,
                () -> taskManager.removeEpic(randomEpicId)
        );

        assertEquals(getMessageTaskNotFoundException(randomEpicId), taskNotFoundException.getMessage());
    }

    @Test
    protected void getAllSubtasksOfEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        final int subtasksCount = 10;
        final List<Subtask> expectedSubtasks = new ArrayList<>(subtasksCount);
        for (int i = 0; i < subtasksCount; i++) {
            Subtask subtask = new Subtask(
                    taskManager.getUniqueSubtaskId(),
                    generator.nextObject(String.class), generator.nextObject(String.class)
            );
            subtask.addRelatedTask(epic);
            expectedSubtasks.add(subtask);
        }

        taskManager.createEpic(epic);
        final List<Subtask> currentSubtasks = taskManager.getAllSubtasksOfEpic(epic.getId());

        assertEquals(expectedSubtasks, currentSubtasks);
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenGetAllSubtasksOfEpicByRandomEpicId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        taskManager.createEpic(epic);
        final int randomEpicId = epic.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getAllSubtasksOfEpic(randomEpicId)
        );

        assertEquals(getMessageTaskNotFoundException(randomEpicId), taskNotFoundException.getMessage());
    }


    @Test
    protected void getUniqueEpicId() {
        assertEquals(1, taskManager.getUniqueEpicId());
    }

    @Test
    protected void getAllSubtasks() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        final int subtasksCount = 10;
        final List<Subtask> expectedSubtasks = new ArrayList<>(subtasksCount);

        for (int i = 0; i < subtasksCount; i++) {
            expectedSubtasks.add(
                    new Subtask(
                            taskManager.getUniqueSubtaskId(),
                            generator.nextObject(String.class), generator.nextObject(String.class), epic
                    )
            );
        }

        taskManager.createEpic(epic);
        expectedSubtasks.forEach(taskManager::createSubtask);
        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();

        assertEquals(expectedSubtasks, actualSubtasks);
    }

    @Test
    protected void shouldReturnEmptyListOfSubtasksWhenSubtasksNotCreated() {
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    protected void shouldReturnEmptyListOfSubtasksAfterRemoveAllSubtasks() {
        final int subtasksCount = 10;
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);
        for (int i = 0; i < subtasksCount; i++) {
            taskManager.createSubtask(
                    new Subtask(
                            generator.nextInt(), generator.nextObject(String.class), generator.nextObject(String.class),
                            epic
                    )
            );
        }
        assertAll(
                () -> assertEquals(subtasksCount, taskManager.getAllSubtasks().size()),
                () -> {
                    taskManager.removeAllSubtasks();
                    assertTrue(taskManager.getAllSubtasks().isEmpty());
                }
        );
    }


    @Test
    protected void getSubtask() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        final Subtask expectedSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        epic.addRelatedTask(expectedSubtask);

        taskManager.createEpic(epic);
        Subtask actualSubtask = taskManager.getSubtask(expectedSubtask.getId());

        assertEquals(expectedSubtask, actualSubtask);
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenGetSubtaskByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        final Subtask expectedSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        epic.addRelatedTask(expectedSubtask);
        taskManager.createEpic(epic);

        final int randomSubtaskId = expectedSubtask.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getSubtask(randomSubtaskId)
        );

        assertEquals(
                getMessageTaskNotFoundException(randomSubtaskId), taskNotFoundException.getMessage()
        );
    }

    @Test
    protected void createSubtask() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );

        taskManager.createSubtask(subtask);

        assertEquals(subtask, taskManager.getSubtask(subtask.getId()));
    }

    @Test
    protected void shouldThrowCreateTaskExceptionWhenSubtaskAlreadyExistWithSameId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        taskManager.createSubtask(subtask);

        final Subtask newSubtask = new Subtask(
                subtask.getId(), generator.nextObject(String.class), generator.nextObject(String.class), epic
        );

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createSubtask(newSubtask));

        assertEquals(getMessageCreateTaskException(subtask.getId()), createTaskException.getMessage());
    }

    @Test
    protected void shouldThrowCreateTaskExceptionWhenTryToCreateSubtaskWithUncreatedEpic() {
        final Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class),
                new Epic(
                        taskManager.getUniqueEpicId(), generator.nextObject(String.class),
                        generator.nextObject(String.class)
                )
        );

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createSubtask(subtask));

        assertEquals(getMessageCreateTaskException(subtask.getId()), createTaskException.getMessage());
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenCreateSubtaskWithOverlappingTime() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask firstSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        firstSubtask.setStartTime(LocalDateTime.now());
        firstSubtask.setDuration(60);

        final Subtask secondSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        secondSubtask.setStartTime(LocalDateTime.now());
        secondSubtask.setDuration(15);

        taskManager.createSubtask(firstSubtask);

        OverlappingTaskTimeException overlappingTaskTimeException = assertThrows(
                OverlappingTaskTimeException.class, () -> taskManager.createSubtask(secondSubtask)
        );
        assertEquals(
                getMessageOverlappingTaskTimeException(secondSubtask.getId()),
                overlappingTaskTimeException.getMessage()
        );
    }

    @Test
    protected void updateSubtask() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int subtaskId = taskManager.getUniqueSubtaskId();
        final Subtask subtask = new Subtask(
                subtaskId, generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        taskManager.createSubtask(subtask);

        final String newSubtaskDescription = generator.nextObject(String.class);
        final TaskStatus taskStatus = TaskStatus.IN_PROGRESS;

        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);
        savedSubtask.setDescription(newSubtaskDescription);
        savedSubtask.setStatus(taskStatus);

        taskManager.updateSubtask(savedSubtask);
        assertEquals(savedSubtask, taskManager.getSubtask(subtaskId));
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedSubtask() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateSubtask(subtask));

        assertEquals(getMessageTaskNotFoundException(subtask.getId()), taskNotFoundException.getMessage());
    }

    @Test
    protected void shouldThrowOverlappingTaskTimeExceptionWhenUpdateSubtaskWithOverlappingTime() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask firstSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        firstSubtask.setStartTime(LocalDateTime.now());
        firstSubtask.setDuration(60);

        final Subtask secondSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        secondSubtask.setStartTime(LocalDateTime.now().minusHours(2));
        secondSubtask.setDuration(15);

        taskManager.createSubtask(firstSubtask);
        taskManager.createSubtask(secondSubtask);

        final Subtask subtaskForUpdate = taskManager.getSubtask(secondSubtask.getId());
        subtaskForUpdate.setStartTime(LocalDateTime.now());

        OverlappingTaskTimeException overlappingTaskTimeException = assertThrows(OverlappingTaskTimeException.class,
                () -> taskManager.updateSubtask(subtaskForUpdate)
        );

        assertEquals(
                getMessageOverlappingTaskTimeException(subtaskForUpdate.getId()),
                overlappingTaskTimeException.getMessage()
        );
    }

    @Test
    protected void removeSubtask() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int subtaskId = taskManager.getUniqueSubtaskId();
        final Subtask subtask = new Subtask(
                subtaskId, generator.nextObject(String.class), generator.nextObject(String.class), epic
        );

        taskManager.createSubtask(subtask);
        taskManager.removeSubtask(subtaskId);

        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    protected void shouldThrowTaskNotFoundExceptionWhenRemoveSubtaskByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );
        taskManager.createSubtask(subtask);
        final int randomSubtaskId = subtask.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.removeSubtask(randomSubtaskId)
        );

        assertEquals(getMessageTaskNotFoundException(randomSubtaskId), taskNotFoundException.getMessage());
    }

    @Test
    protected void getPrioritizedTasks() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int taskDuration = 15;
        final int tasksCount = 20;
        final int tasksCountNullStartTime = 5;
        final Set<LocalDateTime> localDateTimes = new TreeSet<>();
        for (int i = 0; i < tasksCount - tasksCountNullStartTime; i++) {
            localDateTimes.add(LocalDateTime.now().minusHours(i));
        }
        final List<Task> tasks = new ArrayList<>(tasksCount);

        final LocalDateTime[] dtArr = new LocalDateTime[localDateTimes.size()];
        localDateTimes.toArray(dtArr);

        for (int i = 0; i < tasksCount - tasksCountNullStartTime; i++) {
            if (i % 2 == 0)
                tasks.add(new Task(taskManager.getUniqueTaskId(),
                        generator.nextObject(String.class), generator.nextObject(String.class),
                        dtArr[i], taskDuration
                ));
            else
                tasks.add(new Subtask(taskManager.getUniqueSubtaskId(),
                        generator.nextObject(String.class), generator.nextObject(String.class),
                        dtArr[i], taskDuration, epic
                ));
        }
        for (int i = tasksCount - tasksCountNullStartTime; i < tasksCount; i++) {
            if (i % 2 == 0)
                tasks.add(new Task(taskManager.getUniqueTaskId(),
                        generator.nextObject(String.class), generator.nextObject(String.class)
                ));
            else
                tasks.add(new Subtask(taskManager.getUniqueSubtaskId(),
                        generator.nextObject(String.class), generator.nextObject(String.class), epic
                ));
        }

        for (Task task : tasks) {
            if (task instanceof Subtask)
                taskManager.createSubtask((Subtask) task);
            else
                taskManager.createTask(task);
        }

        final List<LocalDateTime> expectedLocalDateTimes = new ArrayList<>(localDateTimes);
        for (int i = 0; i < tasksCountNullStartTime; i++) {
            expectedLocalDateTimes.add(null);
        }
        final List<LocalDateTime> actualLocalDateTimes =
                taskManager.getPrioritizedTasks().stream().map(Task::getStartTime).collect(Collectors.toList());

        actualLocalDateTimes.forEach(System.out::println);
        assertEquals(expectedLocalDateTimes, actualLocalDateTimes);
    }

    @Test
    protected void getUniqueSubtaskId() {
        assertEquals(1, taskManager.getUniqueSubtaskId());
    }

    private String getMessageTaskNotFoundException(int taskId) {
        return "Задача с идентификатором " + taskId + " не найдена!";
    }

    private String getMessageOverlappingTaskTimeException(int taskId) {
        return "Задача с идентификатором " + taskId + " пересекается по времени выполнения!";
    }

    private String getMessageCreateTaskException(int taskId) {
        return "Задача с идентификатором " + taskId + " уже создана!";
    }
}