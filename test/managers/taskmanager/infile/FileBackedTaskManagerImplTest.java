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

class FileBackedTaskManagerImplTest extends TaskManagerTest<FileBackedTaskManagerImpl> {
    private static HistoryManager historyManager = getStubHistoryManager();
    private static String filePath;

    public FileBackedTaskManagerImplTest() {
        super(new FileBackedTaskManagerImpl(historyManager, filePath));
    }

    @BeforeAll
    static void beforeAll() {
        filePath = createTemporaryFile();
    }

    @BeforeEach
    @Override
    protected void beforeEach() {
        historyManager = getStubHistoryManager();
        filePath = createTemporaryFile();
        taskManager = new FileBackedTaskManagerImpl(historyManager, filePath);
    }

    @AfterEach
    @Override
    protected void afterEach() {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldReturnEmptyListOfAllTasksFromEmptyFile() {
        assertAll(
                () -> assertTrue(taskManager.getAllTasks().isEmpty()),
                () -> assertTrue(taskManager.getAllEpics().isEmpty()),
                () -> assertTrue(taskManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    void shouldReturnEmptyListOfSubtasksAfterRestoreFromFileWithTasksAndEpics() {
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
        final TaskManager newTaskManager = new FileBackedTaskManagerImpl(newHistoryManager, filePath);

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
    void shouldReturnEmptyHistoryAfterRestoreFromFileWithEmptyHistoryLine() {
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
        final TaskManager newTaskManager = new FileBackedTaskManagerImpl(newHistoryManager, filePath);

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

    private static HistoryManager getStubHistoryManager() {
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
            filePath = temp.toString();
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(FileBackedTaskMapper.HEADER_OF_FILE + "\n\n\n");
            }
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}