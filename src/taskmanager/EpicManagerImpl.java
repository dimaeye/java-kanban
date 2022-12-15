package taskmanager;

import domain.Epic;
import domain.EpicSetStatusException;
import domain.Subtask;
import domain.TaskNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EpicManagerImpl implements TaskManager<Epic>, EpicManager {

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
    public void create(Epic task) {
        epics.put(task.getId(), task);
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
    public List<Subtask> getAllSubtasksOfEpic(Integer epicId) throws TaskNotFoundException {
        if (epics.containsKey(epicId))
            return epics.get(epicId).getAllSubtasks();
        else
            throw new TaskNotFoundException(epicId);
    }
}
