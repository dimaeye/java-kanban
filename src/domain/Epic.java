package domain;

import domain.exceptions.EpicSetRelatedValueException;
import domain.exceptions.RelatedTaskException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    private LocalDateTime endTime;

    public Epic(int id, String title, String description) {
        super(id, title, description);
    }

    @Override
    public void setStatus(TaskStatus status) throws EpicSetRelatedValueException {
        if (this.status != status)
            throw new EpicSetRelatedValueException(id, "Status");
    }

    @Override
    public void addRelatedTask(Task relatedTask) throws RelatedTaskException, IllegalArgumentException {
        if (!(relatedTask instanceof Subtask))
            throw new IllegalArgumentException("К эпику можно привязать только - " + Subtask.class.getSimpleName());
        if (relatedTask.getAllRelatedTasks().size() == Subtask.MAX_RELATED_TASKS_SIZE
                && relatedTask.getAllRelatedTasks().get(0).getId() == id) {
            Subtask newSubtask = (Subtask) relatedTask;
            subtasks.add(newSubtask);
            verifyEpicStatus();
            refreshTimesByNewRelatedTask(newSubtask);
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

    @Override
    public int getDuration() {
        return super.getDuration();
    }

    @Override
    public void setDuration(int duration) throws EpicSetRelatedValueException {
        throw new EpicSetRelatedValueException(id, "Duration");
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public void setStartTime(LocalDateTime startTime) throws EpicSetRelatedValueException {
        throw new EpicSetRelatedValueException(id, "StartTime");
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    void verifyEpicStatus() {
        if (subtasks.isEmpty()
                || subtasks.stream().filter(subtask -> subtask.status == TaskStatus.NEW).count() == subtasks.size())
            status = TaskStatus.NEW;
        else if (subtasks.stream().filter(subtask -> subtask.status == TaskStatus.DONE).count() == subtasks.size())
            status = TaskStatus.DONE;
        else
            status = TaskStatus.IN_PROGRESS;
    }

    private void refreshTimesByNewRelatedTask(Subtask relatedTask) {
        duration += relatedTask.duration;
        if (startTime == null && relatedTask.startTime != null) {
            startTime = relatedTask.startTime;
            endTime = relatedTask.getEndTime();
        } else if (relatedTask.startTime != null) {
            if (startTime.isAfter(relatedTask.startTime))
                startTime = relatedTask.startTime;
            if (endTime.isBefore(relatedTask.getEndTime()))
                endTime = relatedTask.getEndTime();
        }
    }

    private void refreshTimesByRemovedRelatedTask(Subtask relatedTask) {
        duration -= relatedTask.duration;
        if (startTime.equals(relatedTask.startTime)) {
            //задать новое время startTime
            subtasks.stream()
                    .filter(subtask -> subtask.startTime != null)
                    .min(Comparator.comparingInt(subtask -> subtask.startTime.getNano()))
                    .ifPresent(subtask -> startTime = subtask.startTime);


        }
        if (endTime.equals(relatedTask.getEndTime())) {
            //задать новое время endTime
            subtasks.stream()
                    .filter(subtask -> subtask.startTime != null)
                    .max(Comparator.comparingInt(subtask -> subtask.getEndTime().getNano()))
                    .ifPresent(subtask -> endTime = subtask.getEndTime());
        }
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
