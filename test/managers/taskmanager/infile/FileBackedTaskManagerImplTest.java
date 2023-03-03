package managers.taskmanager.infile;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import managers.taskmanager.TaskManagerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerImplTest extends TaskManagerTest<FileBackedTaskManagerImpl> {
    protected static HistoryManager historyManager = getStubHistoryManager();
    protected static String path;

    @BeforeAll
    static void beforeAll() {
        path = createTemporaryFile();
    }

    @BeforeEach
    @Override
    protected void beforeEach() throws IOException {
        historyManager = getStubHistoryManager();
        path = createTemporaryFile();
        taskManager = new FileBackedTaskManagerImpl(historyManager, path);
    }

    @AfterEach
    @Override
    protected void afterEach() {
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    protected void shouldReturnEmptyListOfAllTasksFromEmptyFile() {
        assertAll(
                () -> assertTrue(taskManager.getAllTasks().isEmpty()),
                () -> assertTrue(taskManager.getAllEpics().isEmpty()),
                () -> assertTrue(taskManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    protected void shouldReturnEmptyListOfSubtasksAfterRestoreFromFileWithTasksAndEpics() {
        final int tasksCount = 10;
        for (int i = 0; i < tasksCount; i++) {
            Task task = new Task(taskManager.getUniqueTaskId(),
                    generator.nextObject(String.class), generator.nextObject(String.class));
            taskManager.createTask(task);
            taskManager.getTask(task.getId());
            Epic epic = new Epic(
                    taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
            );
            taskManager.createEpic(epic);
            taskManager.getEpic(epic.getId());
        }

        final List<Task> expectedTasks = taskManager.getAllTasks();
        final List<Epic> expectedEpics = taskManager.getAllEpics();
        final List<Subtask> expectedSubtasks = taskManager.getAllSubtasks();
        final List<Task> expectedHistory = historyManager.getHistory();

        final HistoryManager newHistoryManager = getStubHistoryManager();
        final TaskManager newTaskManager = restoreTaskManager(newHistoryManager, path);

        assertAll(
                () -> assertEquals(
                        expectedTasks, newTaskManager.getAllTasks(), "Задачи из файла восстановлены верно"
                ),
                () -> assertEquals(
                        expectedEpics, newTaskManager.getAllEpics(), "Эпики из файла восстановлены верно"
                ),
                () -> assertTrue(
                        newTaskManager.getAllSubtasks().isEmpty(), "Список подзадач пуст"
                ),
                () -> assertEquals(
                        expectedSubtasks, newTaskManager.getAllSubtasks(), "Подзадачи из файла восстановлены верно"
                ),
                () -> assertEquals(
                        expectedHistory, newHistoryManager.getHistory(), "История просмотров восстановлена верно"
                )
        );
    }

    @Test
    protected void shouldReturnEmptyHistoryAfterRestoreFromFileWithEmptyHistoryLine() {
        Task task = generator.nextObject(Task.class);
        Epic epic = new Epic(
                taskManager.getUniqueEpicId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );
        Subtask subtask = new Subtask(
                taskManager.getUniqueSubtaskId(),
                generator.nextObject(String.class), generator.nextObject(String.class), epic
        );

        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);

        taskManager.getTask(task.getId());
        taskManager.removeTask(task.getId());

        final List<Epic> expectedEpics = taskManager.getAllEpics();
        final List<Subtask> expectedSubtasks = taskManager.getAllSubtasks();

        final HistoryManager newHistoryManager = getStubHistoryManager();
        final TaskManager newTaskManager = restoreTaskManager(newHistoryManager, path);

        assertAll(
                () -> assertTrue(newHistoryManager.getHistory().isEmpty(), "Пустой список истории"),
                () -> assertTrue(newTaskManager.getAllTasks().isEmpty(), "Пустой список задач"),
                () -> assertEquals(
                        expectedEpics, newTaskManager.getAllEpics(), "Эпики из файла восстановлены верно"
                ),
                () -> assertEquals(
                        expectedSubtasks, newTaskManager.getAllSubtasks(), "Подзадачи из файла восстановлены верно"
                )
        );
    }

    protected TaskManager restoreTaskManager(HistoryManager historyManager, String path) {
        return new FileBackedTaskManagerImpl(historyManager, path);
    }

    protected static HistoryManager getStubHistoryManager() {
        return new HistoryManager() {
            private final List<Task> tasks = new ArrayList<>();

            @Override
            public void add(Task task) {
                tasks.add(task);
            }

            @Override
            public void remove(int id) {
                Optional<Task> optionalTask = tasks.stream().filter(task -> task.getId() == id).findFirst();
                optionalTask.ifPresent(tasks::remove);
            }

            @Override
            public List<Task> getHistory() {
                return tasks.stream().sorted(Comparator.comparingInt(Task::getId)).collect(Collectors.toList());
            }
        };
    }

    private static String createTemporaryFile() {
        try {
            Path temp = Files.createTempFile("", ".tmp");
            path = temp.toString();
            try (FileWriter fileWriter = new FileWriter(path)) {
                fileWriter.write(FileBackedTaskMapper.HEADER_OF_FILE + "\n\n\n");
            }
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}