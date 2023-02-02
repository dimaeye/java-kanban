package managers;

import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.TaskManager;
import managers.taskmanager.infile.FileBackedTaskManagerImpl;

public class Managers {
    private static final HistoryManager historyManager = new InMemoryHistoryManagerImpl();
    private static final TaskManager taskManager = new FileBackedTaskManagerImpl(historyManager, "/tmp/tasks.csv");

    public static TaskManager getDefault() {
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
