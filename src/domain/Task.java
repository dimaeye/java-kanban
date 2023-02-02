package domain;

import domain.exceptions.RelatedTaskException;

import java.util.List;

public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus status = TaskStatus.NEW;

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

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
