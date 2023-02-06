package domain;

import domain.exceptions.EpicSetStatusException;
import domain.exceptions.RelatedTaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(int id, String title, String description) {
        super(id, title, description);
    }

    @Override
    public void setStatus(TaskStatus status) throws EpicSetStatusException {
        if (this.status != status)
            throw new EpicSetStatusException(id);
    }

    @Override
    public void addRelatedTask(Task relatedTask) throws RelatedTaskException, IllegalArgumentException {
        if (!(relatedTask instanceof Subtask))
            throw new IllegalArgumentException("К эпику можно привязать только - " + Subtask.class.getSimpleName());
        if (relatedTask.getAllRelatedTasks().size() == Subtask.MAX_RELATED_TASKS_SIZE
                && relatedTask.getAllRelatedTasks().get(0).getId() == id) {
            subtasks.add((Subtask) relatedTask);
            verifyEpicStatus();
        } else {
            throw new RelatedTaskException("Не удалось добавить подзадачу в эпик");
        }
    }

    @Override
    public void removeRelatedTask(int relatedTaskId) throws RelatedTaskException {
        int indexToRemove = -1;
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).id == relatedTaskId) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0) {
            subtasks.remove(indexToRemove);
            verifyEpicStatus();
        }
    }

    @Override
    public List<Task> getAllRelatedTasks() throws RelatedTaskException {
        return new ArrayList<>(subtasks);
    }

    @Override
    public void removeAllRelatedTasks() throws RelatedTaskException {
        subtasks = new ArrayList<>();
        verifyEpicStatus();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    protected void verifyEpicStatus() {
        if (subtasks.isEmpty()
                || subtasks.stream().filter(subtask -> subtask.status == TaskStatus.NEW).count() == subtasks.size())
            status = TaskStatus.NEW;
        else if (subtasks.stream().filter(subtask -> subtask.status == TaskStatus.DONE).count() == subtasks.size())
            status = TaskStatus.DONE;
        else
            status = TaskStatus.IN_PROGRESS;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasks=" + subtasks +
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
        Epic epic = (Epic) o;
        return subtasks.equals(epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }
}
