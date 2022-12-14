package taskmanager;

import domain.Task;
import domain.TaskNotFoundException;

import java.util.List;

public interface TaskManager<T extends Task> {
    List<T> getAll();

    void removeAll();

    T get(Integer id) throws TaskNotFoundException;

    void create(T task);

    void update(T task) throws TaskNotFoundException;

    void remove(Integer id) throws TaskNotFoundException;
}
