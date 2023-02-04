package managers.taskmanager.infile;

import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskType;
import domain.exceptions.CreateTaskException;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import managers.taskmanager.inmemory.InMemoryTaskManagerImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManagerImpl extends InMemoryTaskManagerImpl implements TaskManager {

    private Path filePath;

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
        save();
    }

    private void save() { //добавить последний сохраненный  Как сохранить только новое? проходить по идентификатору


        try (FileWriter fileWriter = new FileWriter(filePath.toFile(), true)) {
            for (Task task : getAllTasks())
                fileWriter.write(FileBackedTaskMapper.toString(task));
            for (Subtask subtask : getAllSubtasks())
                fileWriter.write(FileBackedTaskMapper.toString(subtask));
            for (Epic epic : getAllEpics())
                fileWriter.write(FileBackedTaskMapper.toString(epic));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadFromFile() throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<FileBackedTaskMapper.TaskWrapper> taskWrappers = new ArrayList<>(lines.size());
        for (String line : lines) {
            taskWrappers.add(FileBackedTaskMapper.fromString(line));
        }
        List<Task> tasks = taskWrappers.stream().map(FileBackedTaskMapper.TaskWrapper::getTask).filter(task ->
                task.getTaskType() == TaskType.TASK
        ).collect(Collectors.toList());
        List<Epic> epics = taskWrappers.stream().filter(taskWrapper ->
                taskWrapper.getTask().getTaskType() == TaskType.EPIC
        ).map(taskWrapper -> (Epic) taskWrapper.getTask()).collect(Collectors.toList());
        List<Subtask> subtasks = taskWrappers.stream().filter(taskWrapper ->
                taskWrapper.getTask().getTaskType() == TaskType.SUBTASK
        ).map(taskWrapper -> {
            Subtask subtask = (Subtask) taskWrapper.getTask();
            subtask.addRelatedTask(epics.get(0));
            return subtask;
        }).collect(Collectors.toList()); //переделать на хэшмапы
    }

}
