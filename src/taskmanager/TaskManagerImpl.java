package taskmanager;

import domain.Task;
import domain.TaskNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManagerImpl implements TaskManager<Task> {

    private final Map<Integer, Task> tasks = new HashMap<>();

    @Override
    public List<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAll() {
        tasks.clear();
    }

    @Override
    public Task get(Integer id) throws TaskNotFoundException {
        if (tasks.containsKey(id))
            return tasks.get(id);
        else
            throw new TaskNotFoundException(id);
    }

    @Override
    public void create(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void update(Task task) throws TaskNotFoundException {
        if (tasks.containsKey(task.getId())) {
            tasks.remove(task.getId());
            tasks.put(task.getId(), task);
        } else {
            throw new TaskNotFoundException(task.getId());
        }
    }

    @Override
    public void remove(Integer id) throws TaskNotFoundException {
        if (tasks.containsKey(id))
            tasks.remove(id);
        else
            throw new TaskNotFoundException(id);
    }
}
