package managers.taskmanager.infile;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskType;
import domain.exceptions.CreateTaskException;
import domain.exceptions.ManagerSaveException;
import domain.exceptions.TaskNotFoundException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import managers.taskmanager.inmemory.InMemoryTaskManagerImpl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileBackedTaskManagerImpl extends InMemoryTaskManagerImpl implements TaskManager {

    private final Path filePath;
    private final List<BufferedOperationTask> bufferedOperationTasks = new LinkedList<BufferedOperationTask>();

    public FileBackedTaskManagerImpl(HistoryManager historyManager, String filePath) {
        super(historyManager);
        this.filePath = Path.of(filePath);
        if (Files.exists(this.filePath)) {
            loadFromFile();
        } else
            try {
                Files.createFile(this.filePath);
                try (FileWriter fileWriter = new FileWriter(this.filePath.toFile())) {
                    fileWriter.write("id,type,name,status,description,epic" + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void removeAllTasks() {
        bufferedOperationTasks.addAll(
                super.getAllTasks().stream()
                        .map(task -> new BufferedOperationTask(OperationType.REMOVE, task))
                        .collect(Collectors.toList())
        );
        super.removeAllTasks();
        save();
    }

    @Override
    public void createTask(Task task) throws CreateTaskException {
        super.createTask(task);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.CREATE, task));
        save();
    }

    @Override
    public void updateTask(Task task) throws TaskNotFoundException {
        super.updateTask(task);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.UPDATE, task));
        save();
    }

    @Override
    public void removeTask(int id) throws TaskNotFoundException {
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.REMOVE, super.getTask(id)));
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllEpics() {
        bufferedOperationTasks.addAll(
                super.getAllEpics().stream()
                        .map(epic -> new BufferedOperationTask(OperationType.REMOVE, epic))
                        .collect(Collectors.toList())
        );
        super.removeAllEpics();
        save();
    }

    @Override
    public void createEpic(Epic epic) throws CreateTaskException {
        super.createEpic(epic);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.CREATE, epic));
        save();
    }

    @Override
    public void updateEpic(Epic epic) throws TaskNotFoundException {
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.UPDATE, epic));
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) throws TaskNotFoundException {
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.REMOVE, super.getEpic(id)));
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllSubtasks() {
        bufferedOperationTasks.addAll(
                super.getAllSubtasks().stream()
                        .map(subtask -> new BufferedOperationTask(OperationType.REMOVE, subtask))
                        .collect(Collectors.toList())
        );
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) throws CreateTaskException {
        super.createSubtask(subtask);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.CREATE, subtask));
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TaskNotFoundException {
        super.updateSubtask(subtask);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.UPDATE, subtask));
        save();
    }

    @Override
    public void removeSubtask(int id) throws TaskNotFoundException {
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.REMOVE, super.getSubtask(id)));
        super.removeSubtask(id);
        save();
    }

    private void save() {
        for (int i = 0; i < bufferedOperationTasks.size(); i++) {
            BufferedOperationTask bufferedOperationTask = bufferedOperationTasks.get(i);
            switch (bufferedOperationTask.task.getTaskType()) {
                case TASK:
                    updateLine(bufferedOperationTask.getTask(), bufferedOperationTask.getOperationType());
                    bufferedOperationTasks.remove(i);
                    break;
                case EPIC:
                    Epic epic = (Epic) bufferedOperationTask.task;
                    if (bufferedOperationTask.getOperationType() == OperationType.CREATE
                            || bufferedOperationTask.getOperationType() == OperationType.REMOVE) {
                        updateLine(epic, bufferedOperationTask.getOperationType());
                        epic.getAllRelatedTasks().forEach(subtask ->
                                updateLine(subtask, bufferedOperationTask.getOperationType())
                        );
                    } else
                        updateLine(epic, OperationType.UPDATE);
                    bufferedOperationTasks.remove(i);
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) bufferedOperationTask.task;
                    Epic relatedEpic = (Epic) subtask.getAllRelatedTasks().get(0);
                    if (relatedEpic != null)
                        updateLine(relatedEpic, OperationType.UPDATE);
                    updateLine(subtask, bufferedOperationTask.getOperationType());
                    bufferedOperationTasks.remove(i);
                    break;
            }

        }
    }

    private <T extends Task> void updateLine(T task, OperationType operationType) {
        File currentFile = filePath.toFile();
        File tmpFile;
        try {
            tmpFile = Files.createTempFile(filePath.getParent(), null, ".tmp").toFile();
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        try (
                BufferedReader fileReader = new BufferedReader(new FileReader(currentFile));
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(tmpFile))
        ) {
            if (fileReader.ready()) {
                fileWriter.write(fileReader.readLine());
                fileWriter.newLine();
            }
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                if (!fileReader.ready()) {
                    fileWriter.newLine();
                    fileWriter.write(line);
                    break;
                }
                switch (operationType) {
                    case CREATE:
                        if (line.isBlank()) {
                            fileWriter.write(FileBackedTaskMapper.toString(task));
                        } else
                            fileWriter.write(line);
                        fileWriter.newLine();
                        break;
                    case UPDATE:
                        if (!line.isBlank()) {
                            if (task.getId() == FileBackedTaskMapper.getTaskIdFromString(line))
                                fileWriter.write(FileBackedTaskMapper.toString(task));
                            else
                                fileWriter.write(line);
                            fileWriter.newLine();
                        }
                        break;
                    case REMOVE:
                        if (!line.isBlank() && task.getId() != FileBackedTaskMapper.getTaskIdFromString(line)) {
                            fileWriter.write(line);
                            fileWriter.newLine();
                        }
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
        if (!currentFile.delete() || !tmpFile.renameTo(currentFile))
            throw new ManagerSaveException("Не удалось обновить в файл " + currentFile.getName());
    }

    private void loadFromFile() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
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

    }

    private enum OperationType {
        CREATE,
        UPDATE,
        REMOVE
    }

    private static class BufferedOperationTask {
        private final OperationType operationType;
        private final Task task;

        public BufferedOperationTask(OperationType operationType, Task task) {
            this.operationType = operationType;
            this.task = task;
        }

        public OperationType getOperationType() {
            return operationType;
        }

        public Task getTask() {
            return task;
        }
    }
}