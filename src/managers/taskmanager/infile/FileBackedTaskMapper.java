package managers.taskmanager.infile;

import domain.*;

import java.util.Optional;

public class FileBackedTaskMapper {
    //csv columns id,type,name,status,description,epic
    private static final int ID_COL_INDEX = 0;
    private static final int TASK_TYPE_COL_INDEX = 1;
    private static final int NAME_COL_INDEX = 2;
    private static final int STATUS_COL_INDEX = 3;
    private static final int DESCRIPTION_COL_INDEX = 4;
    private static final int EPIC_ID_COL_INDEX = 5;
    private static final char ARG_SEPARATOR = ',';

    static <T extends Task> String toString(T task) {
        //id,type,name,status,description,epic
        if (task instanceof Subtask) {
            return String.format("%d,%s,%s,%s,%s,%s%n", task.getId(), task.getTaskType(), task.getTitle(),
                    task.getStatus(), task.getDescription(), task.getAllRelatedTasks().get(0).getId());
        } else
            return String.format("%d,%s,%s,%s,%s,%n", task.getId(), task.getTaskType(), task.getTitle(),
                    task.getStatus(), task.getDescription());
    }

    static TaskWrapper fromString(String line) {
        String[] args = line.split(String.valueOf(ARG_SEPARATOR));
        TaskType taskType = TaskType.valueOf(args[TASK_TYPE_COL_INDEX]);
        TaskWrapper taskWrapper;
        switch (taskType) {
            case TASK:
                taskWrapper = new TaskWrapper(
                        new Task(
                                Integer.parseInt(args[ID_COL_INDEX]), args[NAME_COL_INDEX], args[DESCRIPTION_COL_INDEX]
                        )
                );
                taskWrapper.getTask().setStatus(TaskStatus.valueOf(args[STATUS_COL_INDEX]));
                break;
            case EPIC:
                taskWrapper = new TaskWrapper(
                        new Epic(
                                Integer.parseInt(args[ID_COL_INDEX]), args[NAME_COL_INDEX], args[DESCRIPTION_COL_INDEX]
                        )
                );
                break;
            case SUBTASK:
                taskWrapper = new TaskWrapper(
                        new Subtask(
                                Integer.parseInt(args[ID_COL_INDEX]), args[NAME_COL_INDEX], args[DESCRIPTION_COL_INDEX]
                        ),
                        Integer.parseInt(args[EPIC_ID_COL_INDEX])
                );
                taskWrapper.getTask().setStatus(TaskStatus.valueOf(args[STATUS_COL_INDEX]));
                break;
            default:
                throw new RuntimeException("Неизвестный тип задачи");
        }
        return taskWrapper;
    }

    static class TaskWrapper {
        private final Task task;
        private final Optional<Integer> relatedTaskId;

        public TaskWrapper(Task task) {
            this.task = task;
            relatedTaskId = Optional.empty();
        }

        public TaskWrapper(Task task, int relatedTaskId) {
            this.task = task;
            this.relatedTaskId = Optional.of(relatedTaskId);
        }

        public Task getTask() {
            return task;
        }

        public Optional<Integer> getRelatedTaskId() {
            return relatedTaskId;
        }
    }
}
