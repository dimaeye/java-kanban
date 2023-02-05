package managers.taskmanager.infile;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskType;
import domain.exceptions.CreateTaskException;
import domain.exceptions.ManagerSaveException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import managers.taskmanager.inmemory.InMemoryTaskManagerImpl;

import java.io.FileWriter;
import java.io.IOException;
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
    public void createTask(Task task) throws CreateTaskException {
        super.createTask(task);
        bufferedOperationTasks.add(new BufferedOperationTask(OperationType.CREATE, task));
        save();
    }

    private void save() { //добавить последний сохраненный  Как сохранить только новое? проходить по идентификатору
        try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true)) {
            for (BufferedOperationTask bufferedOperationTask : bufferedOperationTasks) {
                if (bufferedOperationTask.operationType == OperationType.CREATE) {
                    if (bufferedOperationTask.getTask().getTaskType() == TaskType.TASK) {
                        fileWriter.write(FileBackedTaskMapper.toString(bufferedOperationTask.getTask())); //записать до переноса
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            if (lines.get(i).isEmpty())
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