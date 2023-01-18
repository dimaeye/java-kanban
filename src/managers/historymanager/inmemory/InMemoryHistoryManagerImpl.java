package managers.historymanager.inmemory;

import domain.Task;
import managers.historymanager.HistoryManager;

import java.util.List;

public class InMemoryHistoryManagerImpl implements HistoryManager {
    private final TaskLinkedList<Task> history = new TaskLinkedList<>();

    @Override
    public void add(Task task) {
        history.add(task);
    }

    @Override
    public void remove(int id) {
        history.remove(history.get(id));
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }
}
