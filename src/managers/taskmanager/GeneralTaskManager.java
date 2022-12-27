package managers.taskmanager;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;

import java.util.List;

public interface GeneralTaskManager {
    List<Task> getAllTasks();

    void removeAllTasks();

    Task getTask(int id) throws TaskNotFoundException;

    void createTask(Task task) throws CreateTaskException;

    void updateTask(Task task) throws TaskNotFoundException;

    void removeTask(int id) throws TaskNotFoundException;

    int getUniqueTaskId();


    List<Epic> getAllEpics();

    void removeAllEpics();

    Epic getEpic(int id) throws TaskNotFoundException;

    void createEpic(Epic epic) throws CreateTaskException;

    void updateEpic(Epic epic) throws TaskNotFoundException;

    void removeEpic(int id) throws TaskNotFoundException;

    List<Subtask> getAllSubtasksOfEpic(int epicId) throws TaskNotFoundException;

    int getUniqueEpicId();


    List<Subtask> getAllSubtasks();

    void removeAllSubtasks();

    Subtask getSubtask(int id) throws TaskNotFoundException;

    void createSubtask(Subtask subtask) throws CreateTaskException;

    void updateSubtask(Subtask subtask) throws TaskNotFoundException;

    void removeSubtask(int id) throws TaskNotFoundException;

    int getUniqueSubtaskId();
}
