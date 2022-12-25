package managers;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.TaskManager;
import managers.taskmanager.inmemory.InMemoryEpicManagerImpl;
import managers.taskmanager.inmemory.InMemorySubtaskManagerImpl;
import managers.taskmanager.inmemory.InMemoryTaskManagerImpl;

public class Managers {
    private static final HistoryManager historyManager = new InMemoryHistoryManagerImpl();

    private static final TaskManager<Task> taskManager = new InMemoryTaskManagerImpl(historyManager);

    private static final TaskManager<Epic> epicManager = new InMemoryEpicManagerImpl(historyManager);

    private static final TaskManager<Subtask> subtaskManager = new InMemorySubtaskManagerImpl(historyManager);

    public static <T extends Task> TaskManager<T> getDefault(Class<T> classOfT) {
        TaskManager<? extends Task> result;
        if (Epic.class.equals(classOfT)) {
            result = epicManager;
        } else if (Subtask.class.equals(classOfT)) {
            result = subtaskManager;
        } else {
            result = taskManager;
        }
        return (TaskManager<T>) result;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
