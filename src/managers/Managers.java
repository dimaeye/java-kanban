package managers;

import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.GeneralTaskManager;
import managers.taskmanager.inmemory.InMemoryGeneralTaskManagerImpl;

public class Managers {
    private static final HistoryManager historyManager = new InMemoryHistoryManagerImpl();

    private static final GeneralTaskManager generalTaskManager = new InMemoryGeneralTaskManagerImpl(historyManager);

    public static GeneralTaskManager getDefault() {
        return generalTaskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
