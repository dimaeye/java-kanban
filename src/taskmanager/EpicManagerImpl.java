package taskmanager;

import domain.Epic;
import domain.Subtask;
import domain.TaskNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public class EpicManagerImpl implements TaskManager<Epic>, EpicManager {

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final TaskManager<Subtask> subtaskTaskManager = new SubtaskManagerImpl();

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
    public void update(Epic task) throws TaskNotFoundException {
        if (epics.containsKey(task.getId())) {
            epics.remove(task.getId());
            epics.put(task.getId(), task);
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
            return epics.get(epicId).getSubtasks();
        else
            throw new TaskNotFoundException(epicId);
    }

    public TaskManager<Subtask> getSubtaskTaskManager() {
        return subtaskTaskManager;
    }

    public class SubtaskManagerImpl implements TaskManager<Subtask> {

        @Override
        public List<Subtask> getAll() {
            return epics.values().stream()
                    .map(Epic::getSubtasks)
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
            else throw new TaskNotFoundException(id);

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
}
