package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Subtask;
import domain.exceptions.CreateTaskException;
import domain.exceptions.EpicSetStatusException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.EpicManager;
import managers.taskmanager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryEpicManagerImpl implements TaskManager<Epic>, EpicManager {

    private static final AtomicInteger epicId = new AtomicInteger(0);

    private final Map<Integer, Epic> epics = InMemoryDataStore.epics;

    private final HistoryManager historyManager;

    public InMemoryEpicManagerImpl(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Epic> getAll() {
        List<Epic> allEpics = new ArrayList<>(epics.values());
        allEpics.forEach(historyManager::add);
        return allEpics;
    }

    @Override
    public void removeAll() {
        epics.clear();
    }

    @Override
    public Epic get(int id) throws TaskNotFoundException {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            historyManager.add(epic);
            return epic;
        } else
            throw new TaskNotFoundException(id);
    }

    @Override
    public void create(Epic task) throws CreateTaskException {
        if (!epics.containsKey(task.getId()))
            epics.put(task.getId(), task);
        else
            throw new CreateTaskException(task.getId());
    }

    @Override
    public void update(Epic task) throws TaskNotFoundException, EpicSetStatusException {
        if (epics.containsKey(task.getId())) {
            Epic epic = epics.get(task.getId());
            epic.setTitle(task.getTitle());
            epic.setDescription(task.getDescription());
            epic.setStatus(task.getStatus());
        } else {
            throw new TaskNotFoundException(task.getId());
        }
    }

    @Override
    public void remove(int id) throws TaskNotFoundException {
        if (epics.containsKey(id))
            epics.remove(id);
        else
            throw new TaskNotFoundException(id);
    }

    @Override
    public int getUniqueId() {
        return epicId.incrementAndGet();
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(int epicId) throws TaskNotFoundException {
        if (epics.containsKey(epicId)) {
            List<Subtask> subtasks = epics.get(epicId).getAllSubtasks();
            subtasks.forEach(historyManager::add);
            return subtasks;
        } else
            throw new TaskNotFoundException(epicId);
    }
}
