package domain;

import domain.exceptions.RelatedTaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Subtask extends Task {
    private Epic epic;
    public final static int MAX_RELATED_TASKS_SIZE = 1;

    public Subtask(int id, String title, String description, Epic epic) {
        super(id, title, description);
        this.epic = epic;
    }

    public Subtask(int id, String title, String description) {
        super(id, title, description);
    }

    @Override
    public void setStatus(TaskStatus status) {
        if (this.status != status) {
            super.setStatus(status);
            if (epic != null)
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
    public void addRelatedTask(Task relatedTask) throws RelatedTaskException, IllegalArgumentException {
        if (!(relatedTask instanceof Epic))
            throw new IllegalArgumentException("К подзадаче можно привязать только - " + Epic.class.getSimpleName());
        if (epic != null)
            epic.removeRelatedTask(this.id);

        epic = (Epic) relatedTask;
        boolean currentSubtaskIsPresent = epic.getAllRelatedTasks().stream()
                .anyMatch(relatedSubtask -> relatedSubtask.getId() == this.id);
        if (!currentSubtaskIsPresent)
            epic.addRelatedTask(this);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(epic, subtask.epic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epic);
    }
}
