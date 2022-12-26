package managers.taskmanager;

import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;

import java.util.List;

public interface TaskManager<T extends Task> {
    List<T> getAll();

    void removeAll();

    T get(int id) throws TaskNotFoundException;

    void create(T task) throws CreateTaskException;

    void update(T task) throws TaskNotFoundException;

    void remove(int id) throws TaskNotFoundException;

    int getUniqueId();
}
