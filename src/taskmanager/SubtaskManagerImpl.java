package taskmanager;

import domain.Epic;
import domain.Subtask;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SubtaskManagerImpl implements TaskManager<Subtask> {

    private static final AtomicInteger subtaskId = new AtomicInteger(0);

    private final Map<Integer, Epic> epics = DataStore.epics;

    @Override
    public List<Subtask> getAll() {
        return epics.values().stream()
                .map(Epic::getAllSubtasks)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }
    }

    @Override
    public Subtask get(int id) throws TaskNotFoundException {
        List<Subtask> subtasks = getAll();
        Optional<Subtask> optSubtask =
                subtasks.stream().filter(subtask -> Objects.equals(subtask.getId(), id)).findFirst();
        if (optSubtask.isPresent())
            return optSubtask.get();
        else
            throw new TaskNotFoundException(id);

    }

    @Override
    public void create(Subtask task) throws CreateTaskException {
        if (epics.containsKey(task.getEpicId()) && !isSubTaskExist(task)) {
            Epic epic = epics.get(task.getEpicId());
            epic.addSubtask(task);
        } else {
            throw new CreateTaskException(task.getId());
        }
    }

    private boolean isSubTaskExist(Subtask subtask) {
        try {
            this.get(subtask.getId());
            return true;
        } catch (TaskNotFoundException e) {
            return false;
        }
    }

    @Override
    public void update(Subtask task) throws TaskNotFoundException {
        if (epics.containsKey(task.getEpicId()) && isSubTaskExist(task)) {
            Epic epic = epics.get(task.getEpicId());
            Subtask subtask = epic.getSubtask(task.getId());
            subtask.setTitle(task.getTitle());
            subtask.setDescription(task.getDescription());
            subtask.setStatus(task.getStatus());
        } else {
            throw new TaskNotFoundException(task.getId());
        }
    }

    @Override
    public void remove(int id) throws TaskNotFoundException {
        Subtask subtask = get(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(subtask.getId());
    }

    @Override
    public int getUniqueId() {
        return subtaskId.incrementAndGet();
    }
}