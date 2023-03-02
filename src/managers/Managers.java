package managers;

import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.TaskManager;
import managers.taskmanager.http.HttpTaskManager;

public class Managers {
    private static final HistoryManager historyManager = new InMemoryHistoryManagerImpl();
    //    private static final TaskManager taskManager = new FileBackedTaskManagerImpl(historyManager, "/tmp/tasks.csv");
    private static final TaskManager taskManager = new HttpTaskManager(historyManager, "http://localhost:8078");


    public static TaskManager getDefault() {
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
