package managers.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    private T taskManager;

    public TaskManagerTest(T taskManager) {
        this.taskManager = taskManager;
    }


    @Test
    void getAllTasks() {
    }

    @Test
    void removeAllTasks() {
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
}