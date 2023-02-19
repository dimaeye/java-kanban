package domain;

import domain.exceptions.RelatedTaskException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus status = TaskStatus.NEW;
    protected int duration;
    protected LocalDateTime startTime;

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void addRelatedTask(Task relatedTask) throws RelatedTaskException, IllegalArgumentException {
        throw new RelatedTaskException();
    }

    public void removeRelatedTask(int relatedTaskId) throws RelatedTaskException {
        throw new RelatedTaskException();
    }

    public List<Task> getAllRelatedTasks() throws RelatedTaskException {
        throw new RelatedTaskException();
    }

    public void removeAllRelatedTasks() throws RelatedTaskException {
        throw new RelatedTaskException();
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != 0)
            return startTime.plusMinutes(duration);
        else
            return null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && title.equals(task.title)
                && description.equals(task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status);
    }
}
