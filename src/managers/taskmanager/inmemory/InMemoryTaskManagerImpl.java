package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTaskManagerImpl implements TaskManager {

    private static final AtomicInteger taskId = new AtomicInteger(0);

    private final Map<Integer, Task> tasks = InMemoryDataStore.tasks;

    private final Map<Integer, Epic> epics = InMemoryDataStore.epics;

    private final HistoryManager historyManager;

    public InMemoryTaskManagerImpl(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        for (int taskId : tasks.keySet())
            historyManager.remove(taskId);
        tasks.clear();
    }

    @Override
    public Task getTask(int id) throws TaskNotFoundException {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            historyManager.add(task);
            return task;
        } else {
            throw new TaskNotFoundException(id);
        }
    }

    @Override
    public void createTask(Task task) throws CreateTaskException {
        if (!tasks.containsKey(task.getId()))
            tasks.put(task.getId(), task);
        else
            throw new CreateTaskException(task.getId());
    }

    @Override
    public void updateTask(Task task) throws TaskNotFoundException {
        if (tasks.containsKey(task.getId())) {
            Task currentTask = tasks.get(task.getId());
            currentTask.setTitle(task.getTitle());
            currentTask.setDescription(task.getDescription());
            currentTask.setStatus(task.getStatus());
        } else {
            throw new TaskNotFoundException(task.getId());
        }
    }

    @Override
    public void removeTask(int id) throws TaskNotFoundException {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            historyManager.remove(id);
        } else
            throw new TaskNotFoundException(id);
    }

    @Override
    public int getUniqueTaskId() {
        return taskId.incrementAndGet();
    }


    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        for (int epicId : epics.keySet()) {
            epics.get(epicId).getAllRelatedTasks().forEach(t -> historyManager.remove(t.getId()));
            historyManager.remove(epicId);
        }
        epics.clear();
    }

    @Override
    public Epic getEpic(int id) throws TaskNotFoundException {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            historyManager.add(epic);
            return epic;
        } else {
            throw new TaskNotFoundException(id);
        }
    }

    @Override
    public void createEpic(Epic epic) throws CreateTaskException {
        if (!epics.containsKey(epic.getId()))
            epics.put(epic.getId(), epic);
        else
            throw new CreateTaskException(epic.getId());
    }

    @Override
    public void updateEpic(Epic epic) throws TaskNotFoundException {
        if (epics.containsKey(epic.getId())) {
            Epic currentEpic = epics.get(epic.getId());
            currentEpic.setTitle(epic.getTitle());
            currentEpic.setDescription(epic.getDescription());
        } else {
            throw new TaskNotFoundException(epic.getId());
        }
    }

    @Override
    public void removeEpic(int id) throws TaskNotFoundException {
        if (epics.containsKey(id)) {
            historyManager.remove(id);
            epics.get(id).getAllRelatedTasks().forEach(t -> historyManager.remove(t.getId()));
            epics.remove(id);
        } else
            throw new TaskNotFoundException(id);
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(int epicId) throws TaskNotFoundException {
        Optional<Epic> optEpic = epics.values()
                .stream().filter(epic -> epic.getId() == epicId).findFirst();
        if (optEpic.isPresent()) {
            List<Subtask> subtasks = new ArrayList<>();
            for (Task task : optEpic.get().getAllRelatedTasks())
                subtasks.add((Subtask) task);
            return subtasks;
        } else
            throw new TaskNotFoundException(epicId);
    }

    @Override
    public int getUniqueEpicId() {
        return taskId.incrementAndGet();
    }


    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> subtasks = new ArrayList<>();
        for (Epic epic : epics.values()) {
            List<Task> allRelatedTasks = epic.getAllRelatedTasks();
            for (Task task : allRelatedTasks)
                subtasks.add((Subtask) task);
        }
        return subtasks;
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            for (Task relatedTask : epic.getAllRelatedTasks())
                historyManager.remove(relatedTask.getId());
            epic.removeAllRelatedTasks();
        }
    }

    @Override
    public Subtask getSubtask(int id) throws TaskNotFoundException {
        List<Subtask> allSubtasks = getAllSubtasks();
        Optional<Subtask> optSubtask =
                allSubtasks.stream().filter(subtask -> subtask.getId() == id).findFirst();
        if (optSubtask.isPresent()) {
            Subtask subtask = optSubtask.get();
            historyManager.add(subtask);
            return subtask;
        } else {
            throw new TaskNotFoundException(id);
        }
    }

    @Override
    public void createSubtask(Subtask subtask) throws CreateTaskException {
        Epic epicOfNewSubtask = (Epic) subtask.getAllRelatedTasks().get(0);
        if (epics.containsKey(epicOfNewSubtask.getId()) && !isSubTaskExist(subtask)) {
            Epic epic = epics.get(epicOfNewSubtask.getId());
            epic.addRelatedTask(subtask);
        } else {
            throw new CreateTaskException(subtask.getId());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TaskNotFoundException {
        Epic epicOfEditSubtask = (Epic) subtask.getAllRelatedTasks().get(0);
        if (epics.containsKey(epicOfEditSubtask.getId()) && isSubTaskExist(subtask)) {
            Subtask currentSubtask = getSubtask(subtask.getId());
            currentSubtask.setTitle(subtask.getTitle());
            currentSubtask.setDescription(subtask.getDescription());
            currentSubtask.setStatus(subtask.getStatus());
        } else {
            throw new TaskNotFoundException(subtask.getId());
        }
    }

    @Override
    public void removeSubtask(int id) throws TaskNotFoundException {
        Subtask subtask = getSubtask(id);
        Epic epic = epics.get(subtask.getAllRelatedTasks().get(0).getId());
        epic.removeRelatedTask(subtask.getId());
        historyManager.remove(subtask.getId());
    }

    @Override
    public int getUniqueSubtaskId() {
        return taskId.incrementAndGet();
    }

    private boolean isSubTaskExist(Subtask subtask) {
        try {
            this.getSubtask(subtask.getId());
            return true;
        } catch (TaskNotFoundException e) {
            return false;
        }
    }
}
