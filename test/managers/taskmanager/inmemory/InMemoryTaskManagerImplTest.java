package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskStatus;
import domain.exceptions.CreateTaskException;
import domain.exceptions.OverlappingTaskTimeException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerImplTest {

    private final HistoryManager historyManager = new HistoryManager() {
        @Override
        public void add(Task task) {
        }

        @Override
        public void remove(int id) {
        }

        @Override
        public List<Task> getHistory() {
            return null;
        }
    };

    private TaskManager taskManager;

    private final EasyRandom generator = new EasyRandom();

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManagerImpl(historyManager);
    }

    @Test
    void getAllTasks() {
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
    void shouldReturnEmptyListOfTasksWhenTasksNotCreated() {
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfTasksAfterRemoveAllTasks() {
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
    void shouldReturnTaskByIdWhenTaskCreated() {
        final Task expectedTask = generator.nextObject(Task.class);
        taskManager.createTask(expectedTask);

        final Task actualTask = taskManager.getTask(expectedTask.getId());

        assertEquals(expectedTask, actualTask);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenGetTaskByBadId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final int randomTaskId = task.getId() + generator.nextInt();
        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getTask(randomTaskId));

        assertEquals(getMessageTaskNotFoundException(randomTaskId), taskNotFoundException.getMessage());
    }

    @Test
    void shouldThrowCreateTaskExceptionWhenTaskAlreadyExistWithSameId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final Task newTask =
                new Task(task.getId(), generator.nextObject(String.class), generator.nextObject(String.class));

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createTask(newTask));

        assertEquals(getMessageCreateTaskException(task.getId()), createTaskException.getMessage());
    }

    @Test
    void shouldThrowOverlappingTaskTimeExceptionWhenCreateTaskWithOverlappingTime() {
        final Task firstTask = generator.nextObject(Task.class);
        firstTask.setStartTime(LocalDateTime.now());
        firstTask.setDuration(60);

        final Task secondTask = generator.nextObject(Task.class);
        secondTask.setStartTime(LocalDateTime.now());
        secondTask.setDuration(15);

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
    void updateTask() {
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
    void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedTask() {
        final Task task = generator.nextObject(Task.class);

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateTask(task));

        assertEquals(getMessageTaskNotFoundException(task.getId()), taskNotFoundException.getMessage());
    }

    @Test
    void shouldThrowOverlappingTaskTimeExceptionWhenUpdateTaskWithOverlappingTime() {
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
    void removeTask() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        taskManager.removeTask(task.getId());

        Optional<Task> optionalTask =
                taskManager.getAllTasks().stream().filter(t -> t.getId() == task.getId()).findFirst();

        assertTrue(optionalTask.isEmpty());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenRemoveTaskByRandomId() {
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
    void getUniqueTaskId() {
        assertEquals(1, taskManager.getUniqueTaskId());
    }

    @Test
    void getAllEpics() {
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
    void shouldReturnEmptyListOfEpicsWhenEpicsNotCreated() {
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfEpicsAndSubtasksAfterRemoveAllEpics() {
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
    void getEpic() {
        final Epic expectedEpic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );
        taskManager.createEpic(expectedEpic);

        final Epic actualEpic = taskManager.getEpic(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenGetEpicByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        final int randomEpicId = epic.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getEpic(randomEpicId)
        );

        assertEquals(getMessageTaskNotFoundException(randomEpicId), taskNotFoundException.getMessage());
    }


    @Test
    void createEpic() {
        final Epic expectedEpic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );
        taskManager.createEpic(expectedEpic);

        final Epic actualEpic = taskManager.getEpic(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    void shouldThrowCreateTaskExceptionWhenEpicAlreadyExistWithSameId() {
        final Task task = generator.nextObject(Task.class);
        taskManager.createTask(task);

        final Task newTask =
                new Task(task.getId(), generator.nextObject(String.class), generator.nextObject(String.class));

        CreateTaskException createTaskException = assertThrows(CreateTaskException.class,
                () -> taskManager.createTask(newTask));

        assertEquals(getMessageCreateTaskException(task.getId()), createTaskException.getMessage());
    }

    @Test
    void updateEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
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
    void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateTask(epic));

        assertEquals(getMessageTaskNotFoundException(epic.getId()), taskNotFoundException.getMessage());
    }

    @Test
    void removeEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );
        taskManager.createEpic(epic);

        taskManager.removeEpic(epic.getId());

        Optional<Epic> optionalTask =
                taskManager.getAllEpics().stream().filter(t -> t.getId() == epic.getId()).findFirst();

        assertTrue(optionalTask.isEmpty());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenRemoveEpicByRandomId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
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
    void getAllSubtasksOfEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );

        final int subtasksCount = 10;
        final List<Subtask> expectedSubtasks = new ArrayList<>(subtasksCount);
        for (int i = 0; i < subtasksCount; i++) {
            Subtask subtask = new Subtask(
                    taskManager.getUniqueSubtaskId(),
                    generator.nextObject(String.class),
                    generator.nextObject(String.class)
            );
            subtask.addRelatedTask(epic);
            expectedSubtasks.add(subtask);
        }

        taskManager.createEpic(epic);
        final List<Subtask> currentSubtasks = taskManager.getAllSubtasksOfEpic(epic.getId());

        assertEquals(expectedSubtasks, currentSubtasks);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenGetAllSubtasksOfEpicByRandomEpicId() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(),
                generator.nextObject(String.class),
                generator.nextObject(String.class)
        );

        taskManager.createEpic(epic);
        final int randomEpicId = epic.getId() + generator.nextInt();

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.getAllSubtasksOfEpic(randomEpicId)
        );

        assertEquals(getMessageTaskNotFoundException(randomEpicId), taskNotFoundException.getMessage());
    }


    @Test
    void getUniqueEpicId() {
        assertEquals(1, taskManager.getUniqueEpicId());
    }

    @Test
    void getAllSubtasks() {
    }

    @Test
    void shouldReturnEmptyListOfSubtasksWhenSubtasksNotCreated() {
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfSubtasksAfterRemoveAllSubtasks() {
        int subtasksCount = 10;
        Epic epic = new Epic(
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
        assertAll(
                () -> assertEquals(subtasksCount, taskManager.getAllSubtasks().size()),
                () -> {
                    taskManager.removeAllSubtasks();
                    assertTrue(taskManager.getAllSubtasks().isEmpty());
                }
        );
    }


    @Test
    void getSubtask() {
    }

    @Test
    void createSubtask() {
    }

    @Test
    void updateSubtask() {
    }

    @Test
    void removeSubtask() {
    }

    @Test
    void getPrioritizedTasks() {
    }

    @Test
    void getUniqueSubtaskId() {
    }

    @Test
    void setInitialUniqueId() {
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