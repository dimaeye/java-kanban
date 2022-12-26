package managers.taskmanager.inmemory;

import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTaskManagerImpl implements TaskManager<Task> {

    private static final AtomicInteger taskId = new AtomicInteger(0);

    private final Map<Integer, Task> tasks = InMemoryDataStore.tasks;

    private final HistoryManager historyManager;

    public InMemoryTaskManagerImpl(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getAll() {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        allTasks.forEach(historyManager::add);
        return allTasks;
    }

    @Override
    public void removeAll() {
        tasks.clear();
    }

    @Override
    public Task get(int id) throws TaskNotFoundException {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            historyManager.add(task);
            return task;
        } else
            throw new TaskNotFoundException(id);
    }

    @Override
    public void create(Task task) throws CreateTaskException {
        if (!tasks.containsKey(task.getId()))
            tasks.put(task.getId(), task);
        else
            throw new CreateTaskException(task.getId());
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
    public void remove(int id) throws TaskNotFoundException {
        if (tasks.containsKey(id))
            tasks.remove(id);
        else
            throw new TaskNotFoundException(id);
    }

    @Override
    public int getUniqueId() {
        return taskId.incrementAndGet();
    }
}
