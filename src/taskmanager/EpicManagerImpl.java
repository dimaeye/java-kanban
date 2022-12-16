package taskmanager;

import domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EpicManagerImpl implements TaskManager<Epic>, EpicManager {

    private static final AtomicInteger epicId = new AtomicInteger(0);

    private final Map<Integer, Epic> epics = DataStore.epics;

    @Override
    public List<Epic> getAll() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAll() {
        epics.clear();
    }

    @Override
    public Epic get(Integer id) throws TaskNotFoundException {
        if (epics.containsKey(id))
            return epics.get(id);
        else
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
    public void remove(Integer id) throws TaskNotFoundException {
        if (epics.containsKey(id))
            epics.remove(id);
        else
            throw new TaskNotFoundException(id);
    }

    @Override
    public Integer getUniqueId() {
        return epicId.incrementAndGet();
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(Integer epicId) throws TaskNotFoundException {
        if (epics.containsKey(epicId))
            return epics.get(epicId).getAllSubtasks();
        else
            throw new TaskNotFoundException(epicId);
    }
}
