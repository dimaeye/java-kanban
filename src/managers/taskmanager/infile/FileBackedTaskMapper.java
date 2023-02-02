package managers.taskmanager.infile;

import domain.Subtask;
import domain.Task;

public class FileBackedTaskMapper {
    static <T extends Task> String toString(T task) {
        //id,type,name,status,description,epic
        if (task instanceof Subtask) {
            return String.format("%d,%s,%s,%s,%s,%s%n", task.getId(), task.getTaskType(), task.getTitle(),
                    task.getStatus(), task.getDescription(), task.getAllRelatedTasks().get(0).getId());
        } else
            return String.format("%d,%s,%s,%s,%s,%n", task.getId(), task.getTaskType(), task.getTitle(),
                    task.getStatus(), task.getDescription());
    }

    static <T extends Task> T fromString(String line) {
        // TODO: 02.02.2023
        throw new RuntimeException();
    }
}
