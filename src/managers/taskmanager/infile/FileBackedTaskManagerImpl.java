package managers.taskmanager.infile;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskType;
import domain.exceptions.*;
import managers.historymanager.HistoryManager;
import managers.taskmanager.inmemory.InMemoryTaskManagerImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManagerImpl extends InMemoryTaskManagerImpl {
    private final Path filePath;

    public FileBackedTaskManagerImpl(HistoryManager historyManager, String filePath) {
        super(historyManager);
        this.filePath = Path.of(filePath);
        if (Files.exists(this.filePath)) {
            loadFromFile();
        } else
            try {
                Files.createFile(this.filePath);
                try (FileWriter fileWriter = new FileWriter(this.filePath.toFile())) {
                    fileWriter.write(FileBackedTaskMapper.HEADER_OF_FILE + "\n\n\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public Task getTask(int id) throws TaskNotFoundException {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public void createTask(Task task) throws CreateTaskException, OverlappingTaskTimeException {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) throws TaskNotFoundException, OverlappingTaskTimeException {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) throws TaskNotFoundException {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public Epic getEpic(int id) throws TaskNotFoundException {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public void createEpic(Epic epic) throws CreateTaskException {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) throws TaskNotFoundException {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) throws TaskNotFoundException {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public Subtask getSubtask(int id) throws TaskNotFoundException {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) throws CreateTaskException, OverlappingTaskTimeException {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TaskNotFoundException, OverlappingTaskTimeException {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) throws TaskNotFoundException {
        super.removeSubtask(id);
        save();
    }

    private void save() {
        File currentFile = filePath.toFile();
        File tmpFile;

        try {
            tmpFile = Files.createTempFile(filePath.getParent(), null, ".tmp").toFile();
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(tmpFile))) {
            List<Task> tasks = getAllTasks();
            List<Epic> epics = getAllEpics();

            fileWriter.write(FileBackedTaskMapper.HEADER_OF_FILE);
            fileWriter.newLine();

            for (Task task : tasks) {
                fileWriter.write(FileBackedTaskMapper.toString(task));
                fileWriter.newLine();
            }
            for (Epic epic : epics) {
                fileWriter.write(FileBackedTaskMapper.toString(epic));
                fileWriter.newLine();
                for (Task subtask : epic.getAllRelatedTasks()) {
                    fileWriter.write(FileBackedTaskMapper.toString((Subtask) subtask));
                    fileWriter.newLine();
                }
            }

            fileWriter.newLine();
            String historyString = FileBackedHistoryMapper.historyToString(historyManager);
            if (!historyString.isBlank())
                fileWriter.write(historyString);
            else
                fileWriter.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
        if (!currentFile.delete() || !tmpFile.renameTo(currentFile))
            throw new ManagerSaveException("Не удалось обновить файл " + currentFile.getName());
    }

    private void loadFromFile() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }

        List<FileBackedTaskMapper.TaskWrapper> allTaskWrappers = new ArrayList<>(lines.size());
        int initialUniqueId = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank())
                break;
            FileBackedTaskMapper.TaskWrapper taskWrapper = FileBackedTaskMapper.fromString(lines.get(i));
            if (taskWrapper.getTask().getId() > initialUniqueId)
                initialUniqueId = taskWrapper.getTask().getId();
            allTaskWrappers.add(taskWrapper);
        }
        super.setInitialUniqueId(initialUniqueId);

        allTaskWrappers.stream()
                .map(FileBackedTaskMapper.TaskWrapper::getTask)
                .filter(task -> task.getTaskType() == TaskType.TASK)
                .forEach(super::createTask);

        Map<Integer, Epic> epics = allTaskWrappers.stream()
                .filter(taskWrapper -> taskWrapper.getTask().getTaskType() == TaskType.EPIC)
                .map(taskWrapper -> (Epic) taskWrapper.getTask())
                .collect(Collectors.toMap(Epic::getId, Function.identity()));
        allTaskWrappers.stream()
                .filter(taskWrapper -> taskWrapper.getTask().getTaskType() == TaskType.SUBTASK)
                .forEach(taskWrapper -> {
                    Subtask subtask = (Subtask) taskWrapper.getTask();
                    subtask.addRelatedTask(epics.get(taskWrapper.getRelatedTaskId().get()));
                });

        epics.values().forEach(super::createEpic);
        loadHistoryFromFile();
    }

    private void loadHistoryFromFile() {
        List<Task> allTasks = super.getAllTasks();
        List<Epic> allEpics = super.getAllEpics();
        List<Subtask> allSubtasks = super.getAllSubtasks();
        int skipLinesCount = allTasks.size() + allEpics.size() + allSubtasks.size() + 2; //2 is header line plus delimiter

        try (Stream<String> lines = Files.lines(filePath)) {
            String line = lines.skip(skipLinesCount).findFirst().get();
            List<Integer> taskIds = FileBackedHistoryMapper.historyFromString(line);
            Stream.of(allTasks, allEpics, allSubtasks)
                    .flatMap(Collection::stream)
                    .filter(task -> taskIds.contains(task.getId()))
                    .forEach(historyManager::add);
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }

    }
}