package managers;

import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.TaskManager;
import managers.taskmanager.http.HttpTaskManager;
import presenter.server.KVServer;

public class Managers {
    private static final HistoryManager historyManager = new InMemoryHistoryManagerImpl();
    private static final TaskManager taskManager = new HttpTaskManager(
            historyManager, "http://localhost:" + KVServer.PORT
    );


    public static TaskManager getDefault() {
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
