package domain;

import domain.exceptions.RelatedTaskException;

import java.util.ArrayList;
import java.util.List;

public class Subtask extends Task {
    private final Epic epic;
    public final static int MAX_RELATED_TASKS_SIZE = 1;

    public Subtask(int id, String title, String description, Epic epic) {
        super(id, title, description);
        this.epic = epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        if (this.status != status) {
            super.setStatus(status);
            epic.verifyEpicStatus();
        }
    }

    @Override
    public List<Task> getAllRelatedTasks() throws RelatedTaskException {
        List<Task> result = new ArrayList<>();
        result.add(epic);
        return result;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epic.getId() +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
