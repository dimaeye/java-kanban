package managers.taskmanager.infile;

import domain.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class FileBackedTaskMapper {
    static final String HEADER_OF_FILE = "id,type,name,status,description,epic,startTime,duration,endTime";
    private static final int ID_COL_INDEX = 0;
    private static final int TASK_TYPE_COL_INDEX = 1;
    private static final int NAME_COL_INDEX = 2;
    private static final int STATUS_COL_INDEX = 3;
    private static final int DESCRIPTION_COL_INDEX = 4;
    private static final int EPIC_ID_COL_INDEX = 5;
    private static final int START_TIME_COL_INDEX = 6;
    private static final int DURATION_COL_INDEX = 7;
    private static final int END_TIME_COL_INDEX = 8;
    private static final char ARG_SEPARATOR = ',';
    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private FileBackedTaskMapper() {
    }

    static <T extends Task> String toString(T task) {
        //id,type,name,status,description,epic,startTime,duration,endTime
        if (task instanceof Subtask) {
            return String.format(
                    "%d,%s,%s,%s,%s,%s,%s,%d,%s",
                    task.getId(), task.getTaskType(), task.getTitle(), task.getStatus(), task.getDescription(),
                    task.getAllRelatedTasks().get(0).getId(),
                    task.getStartTime() != null ? task.getStartTime().format(formatter) : "", task.getDuration(),
                    task.getEndTime() != null ? task.getEndTime().format(formatter) : ""
            );
        } else
            return String.format(
                    "%d,%s,%s,%s,%s,,%s,%d,%s",
                    task.getId(), task.getTaskType(), task.getTitle(), task.getStatus(), task.getDescription(),
                    task.getStartTime() != null ? task.getStartTime().format(formatter) : "", task.getDuration(),
                    task.getEndTime() != null ? task.getEndTime().format(formatter) : ""
            );
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
                Task task = taskWrapper.getTask();
                task.setStatus(TaskStatus.valueOf(args[STATUS_COL_INDEX]));
                if (!args[START_TIME_COL_INDEX].isBlank())
                    task.setStartTime(LocalDateTime.parse(args[START_TIME_COL_INDEX], formatter));
                task.setDuration(Integer.parseInt(args[DURATION_COL_INDEX]));
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
                Task subtask = taskWrapper.getTask();
                subtask.setStatus(TaskStatus.valueOf(args[STATUS_COL_INDEX]));
                if (!args[START_TIME_COL_INDEX].isBlank())
                    subtask.setStartTime(LocalDateTime.parse(args[START_TIME_COL_INDEX], formatter));
                subtask.setDuration(Integer.parseInt(args[DURATION_COL_INDEX]));
                break;
            default:
                throw new RuntimeException("Неизвестный тип задачи");
        }
        return taskWrapper;
    }

    static int getTaskIdFromString(String line) {
        return Integer.parseInt(line.substring(0, line.indexOf(ARG_SEPARATOR)));
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
