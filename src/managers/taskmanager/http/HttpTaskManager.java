package managers.taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.Epic;
import domain.Subtask;
import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.infile.FileBackedTaskManagerImpl;
import presenter.client.KVTaskClient;
import presenter.client.KVTaskClientImpl;
import presenter.config.GsonConfig;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpTaskManager extends FileBackedTaskManagerImpl {
    private KVTaskClient kvTaskClient;
    private final Type tasksTypeToken = new TypeToken<List<Task>>() {
    }.getType();
    private final Type epicsTypeToken = new TypeToken<List<Epic>>() {
    }.getType();
    private final Type historyTypeToken = new TypeToken<List<Integer>>() {
    }.getType();

    public HttpTaskManager(HistoryManager historyManager, String path) {
        super(historyManager, path);
    }

    @Override
    protected void save() {
        Gson gson = GsonConfig.getGson();
        kvTaskClient.put(Keys.TASKS.name(), gson.toJson(getAllTasks()));
        kvTaskClient.put(Keys.EPICS.name(), gson.toJson(getAllEpics()));
        kvTaskClient.put(
                Keys.HISTORY.name(),
                gson.toJson(historyManager.getHistory().stream().map(Task::getId).collect(Collectors.toList()))
        );
    }

    @Override
    protected void loadFromStorage() {
        kvTaskClient = new KVTaskClientImpl(this.path);
        Gson gson = GsonConfig.getGson();
        try {
            List<Task> tasks = gson.fromJson(kvTaskClient.load(Keys.TASKS.name()), tasksTypeToken);
            tasks.forEach(this::createTask);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            List<Epic> epics = gson.fromJson(kvTaskClient.load(Keys.EPICS.name()), epicsTypeToken);
            epics.forEach(epic -> {
                List<Task> subtasks = epic.getAllRelatedTasks();
                subtasks.forEach(s -> s.addRelatedTask(epic));
                createEpic(epic);
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        loadHistory();
    }

    private void loadHistory() {
        Gson gson = GsonConfig.getGson();

        List<Task> allTasks = super.getAllTasks();
        List<Epic> allEpics = super.getAllEpics();
        List<Subtask> allSubtasks = super.getAllSubtasks();

        try {
            List<Integer> history = gson.fromJson(kvTaskClient.load(Keys.HISTORY.name()), historyTypeToken);
            Stream.of(allTasks, allEpics, allSubtasks)
                    .flatMap(Collection::stream)
                    .filter(task -> history.contains(task.getId()))
                    .forEach(historyManager::add);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private enum Keys {
        TASKS, EPICS, SUBTASKS, HISTORY
    }
}
