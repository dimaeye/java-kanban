package taskmanager;

import domain.Epic;
import domain.Subtask;
import domain.TaskNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class SubtaskManagerImpl implements TaskManager<Subtask> {

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
    public Subtask get(Integer id) throws TaskNotFoundException {
        List<Subtask> subtasks = getAll();
        Optional<Subtask> optSubtask =
                subtasks.stream().filter(subtask -> Objects.equals(subtask.getId(), id)).findFirst();
        if (optSubtask.isPresent())
            return optSubtask.get();
        else
            throw new TaskNotFoundException(id);

    }

    @Override
    public void create(Subtask task) {
        Epic epic = epics.get(task.getEpicId());
        epic.addSubtask(task);
    }

    @Override
    public void update(Subtask task) throws TaskNotFoundException {
        Epic epic = epics.get(task.getEpicId());
        epic.removeSubtask(task.getId());
        epic.addSubtask(task);
    }

    @Override
    public void remove(Integer id) throws TaskNotFoundException {
        Subtask subtask = get(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(subtask.getId());
    }
}