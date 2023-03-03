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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpTaskManager extends FileBackedTaskManagerImpl {
    private KVTaskClient kvTaskClient;

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
        final int[] initialUniqueId = {0};

        List<Task> tasks = new ArrayList<>();
        List<Epic> epics = new ArrayList<>();
        List<Integer> historyIds = new ArrayList<>();

        try {
            tasks = gson.fromJson(
                    kvTaskClient.load(Keys.TASKS.name()), new TypeToken<List<Task>>() {
                    }.getType()
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            epics = gson.fromJson(
                    kvTaskClient.load(Keys.EPICS.name()), new TypeToken<List<Epic>>() {
                    }.getType()
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            historyIds = gson.fromJson(
                    kvTaskClient.load(Keys.HISTORY.name()), new TypeToken<List<Integer>>() {
                    }.getType()
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }

        tasks.forEach(task -> {
            createTask(task);
            if (initialUniqueId[0] < task.getId())
                initialUniqueId[0] = task.getId();
        });
        epics.forEach(epic -> {
            if (initialUniqueId[0] < epic.getId())
                initialUniqueId[0] = epic.getId();
            List<Task> subtasks = epic.getAllRelatedTasks();
            subtasks.forEach(s -> {
                s.addRelatedTask(epic);
                if (initialUniqueId[0] < s.getId())
                    initialUniqueId[0] = s.getId();
            });
            createEpic(epic);
        });

        super.setInitialUniqueId(initialUniqueId[0]);
        setHistory(historyIds);
    }

    private void setHistory(List<Integer> historyIds) {
        List<Task> allTasks = super.getAllTasks();
        List<Epic> allEpics = super.getAllEpics();
        List<Subtask> allSubtasks = super.getAllSubtasks();

        try {
            Stream.of(allTasks, allEpics, allSubtasks)
                    .flatMap(Collection::stream)
                    .filter(task -> historyIds.contains(task.getId()))
                    .forEach(historyManager::add);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private enum Keys {
        TASKS, EPICS, SUBTASKS, HISTORY
    }
}
