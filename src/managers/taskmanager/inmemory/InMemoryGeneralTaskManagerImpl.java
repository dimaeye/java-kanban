package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.GeneralTaskManager;
import managers.taskmanager.TaskManager;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryGeneralTaskManagerImpl implements GeneralTaskManager {

    private final Map<Type, TaskManager<? extends Task>> taskManagers;
    private final HistoryManager historyManager;

    public InMemoryGeneralTaskManagerImpl(HistoryManager historyManager) {
        this.historyManager = historyManager;
        taskManagers = new HashMap<>();
        taskManagers.put(Task.class, new InMemoryTaskManagerImpl());
        taskManagers.put(Epic.class, new InMemoryEpicManagerImpl());
        taskManagers.put(Subtask.class, new InMemorySubtaskManagerImpl());
    }

    @Override
    public List<Task> getAllTasks() {
        return getTaskManager(Task.class).getAll();
    }

    @Override
    public void removeAllTasks() {
        getTaskManager(Task.class).removeAll();
    }

    @Override
    public Task getTask(int id) throws TaskNotFoundException {
        Task task = getTaskManager(Task.class).get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public void createTask(Task task) throws CreateTaskException {
        getTaskManager(Task.class).create(task);
    }

    @Override
    public void updateTask(Task task) throws TaskNotFoundException {
        getTaskManager(Task.class).update(task);
    }

    @Override
    public void removeTask(int id) throws TaskNotFoundException {
        getTaskManager(Task.class).remove(id);
    }

    @Override
    public int getUniqueTaskId() {
        return getTaskManager(Task.class).getUniqueId();
    }


    @Override
    public List<Epic> getAllEpics() {
        return getTaskManager(Epic.class).getAll();
    }

    @Override
    public void removeAllEpics() {
        getTaskManager(Epic.class).removeAll();
    }

    @Override
    public Epic getEpic(int id) throws TaskNotFoundException {
        Epic epic = getTaskManager(Epic.class).get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void createEpic(Epic epic) throws CreateTaskException {
        getTaskManager(Epic.class).create(epic);
    }

    @Override
    public void updateEpic(Epic epic) throws TaskNotFoundException {
        getTaskManager(Epic.class).update(epic);
    }

    @Override
    public void removeEpic(int id) throws TaskNotFoundException {
        getTaskManager(Epic.class).remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(int epicId) throws TaskNotFoundException {
        Optional<Epic> optEpic = getTaskManager(Epic.class).getAll()
                .stream().filter(epic -> epic.getId() == epicId).findFirst();
        if (optEpic.isPresent())
            return optEpic.get().getAllSubtasks();
        else
            throw new TaskNotFoundException(epicId);
    }

    @Override
    public int getUniqueEpicId() {
        return getTaskManager(Epic.class).getUniqueId();
    }


    @Override
    public List<Subtask> getAllSubtasks() {
        return getTaskManager(Subtask.class).getAll();
    }

    @Override
    public void removeAllSubtasks() {
        getTaskManager(Subtask.class).removeAll();
    }

    @Override
    public Subtask getSubtask(int id) throws TaskNotFoundException {
        Subtask subtask = getTaskManager(Subtask.class).get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) throws CreateTaskException {
        getTaskManager(Subtask.class).create(subtask);
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TaskNotFoundException {
        getTaskManager(Subtask.class).update(subtask);
    }

    @Override
    public void removeSubtask(int id) throws TaskNotFoundException {
        getTaskManager(Subtask.class).remove(id);
    }

    @Override
    public int getUniqueSubtaskId() {
        return getTaskManager(Subtask.class).getUniqueId();
    }

    @SuppressWarnings("unchecked")
    private <T extends Task> TaskManager<T> getTaskManager(Class<T> classOfT) {
        return (TaskManager<T>) taskManagers.get(classOfT);
    }
}
