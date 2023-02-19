package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryTaskManagerImplTest {

    private HistoryManager historyManager = new HistoryManager() {
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

    private EasyRandom generator = new EasyRandom();

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManagerImpl(historyManager);
    }

    @Test
    void shouldReturnEmptyListOfTasksWhenTasksNotCreated() {
        Assertions.assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfEpicsWhenEpicsNotCreated() {
        Assertions.assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfSubtasksWhenSubtasksNotCreated() {
        Assertions.assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldReturnEmptyListOfTasksAfterRemoveAllTasks() {
        int tasksCount = 10;
        generator.objects(Task.class, tasksCount).forEach(task -> {
            task.setStartTime(null);
            taskManager.createTask(task);
        });
        Assertions.assertAll(
                () -> Assertions.assertEquals(tasksCount, taskManager.getAllTasks().size()),
                () -> {
                    taskManager.removeAllTasks();
                    Assertions.assertTrue(taskManager.getAllTasks().isEmpty());
                }
        );
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
        Assertions.assertAll(
                () -> Assertions.assertEquals(subtasksCount, taskManager.getAllSubtasks().size()),
                () -> {
                    taskManager.removeAllSubtasks();
                    Assertions.assertTrue(taskManager.getAllSubtasks().isEmpty());
                }
        );
    }

    @Test
    void shouldReturnEmptyListOfEpicsAndSubtasksAfterRemoveAllEpics() {
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

        taskManager.removeAllEpics();

        Assertions.assertAll(
                () -> Assertions.assertTrue(taskManager.getAllEpics().isEmpty()),
                () -> Assertions.assertTrue(taskManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    void getTask() {
    }

    @Test
    void createTask() {
    }

    @Test
    void updateTask() {
    }

    @Test
    void removeTask() {
    }

    @Test
    void getUniqueTaskId() {
    }

    @Test
    void getAllEpics() {
    }

    @Test
    void removeAllEpics() {
    }

    @Test
    void getEpic() {
    }

    @Test
    void createEpic() {
    }

    @Test
    void updateEpic() {
    }

    @Test
    void removeEpic() {
    }

    @Test
    void getAllSubtasksOfEpic() {
    }

    @Test
    void getUniqueEpicId() {
    }

    @Test
    void getAllSubtasks() {
    }

    @Test
    void removeAllSubtasks() {
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
}