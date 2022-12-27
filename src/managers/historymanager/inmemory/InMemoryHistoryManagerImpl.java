package managers.historymanager.inmemory;

import domain.Task;
import managers.historymanager.HistoryManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManagerImpl implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private static final List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (history.size() == MAX_HISTORY_SIZE)
            history.remove(0);
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
