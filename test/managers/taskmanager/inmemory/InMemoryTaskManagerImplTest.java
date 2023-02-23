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
import java.util.*;
import java.util.stream.Collectors;

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
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        taskManager.createEpic(expectedEpic);

        final Epic actualEpic = taskManager.getEpic(expectedEpic.getId());

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenGetEpicByRandomId() {
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
    void createEpic() {
        final Epic expectedEpic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
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
    void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedEpic() {
        final Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        TaskNotFoundException taskNotFoundException = assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateTask(epic));

        assertEquals(getMessageTaskNotFoundException(epic.getId()), taskNotFoundException.getMessage());
    }

    @Test
    void removeEpic() {
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
    void shouldThrowTaskNotFoundExceptionWhenRemoveEpicByRandomId() {
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
    void getAllSubtasksOfEpic() {
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
    void shouldThrowTaskNotFoundExceptionWhenGetAllSubtasksOfEpicByRandomEpicId() {
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
    void getUniqueEpicId() {
        assertEquals(1, taskManager.getUniqueEpicId());
    }

    @Test
    void getAllSubtasks() {
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
    void shouldReturnEmptyListOfSubtasksWhenSubtasksNotCreated() {
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfSubtasksAfterRemoveAllSubtasks() {
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
    void getSubtask() {
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
    void shouldThrowTaskNotFoundExceptionWhenGetSubtaskByRandomId() {
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
    void createSubtask() {
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
    void shouldThrowCreateTaskExceptionWhenSubtaskAlreadyExistWithSameId() {
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
    void shouldThrowCreateTaskExceptionWhenTryToCreateSubtaskWithUncreatedEpic() {
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
    void updateSubtask() {
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
    void shouldThrowTaskNotFoundExceptionWhenUpdateUncreatedSubtask() {
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
    void removeSubtask() {
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
    void shouldThrowTaskNotFoundExceptionWhenRemoveSubtaskByRandomId() {
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
    void getPrioritizedTasks() {
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
    void getUniqueSubtaskId() {
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