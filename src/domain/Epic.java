package domain;

import domain.exceptions.EpicSetStatusException;
import domain.exceptions.TaskNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(int id, String title, String description) {
        super(id, title, description);
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
        verifyEpicStatus();
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

    public Subtask getSubtask(int id) throws TaskNotFoundException {
        Optional<Subtask> optSubtask = subtasks.stream().filter(subtask -> subtask.getId() == id).findFirst();
        if (optSubtask.isPresent())
            return optSubtask.get();
        else
            throw new TaskNotFoundException(id);
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubtask(Subtask subtask) {
        if (Objects.equals(subtask.getEpic().getId(), id)) {
            subtasks.add(subtask);
            verifyEpicStatus();
        }
    }

    public void removeSubtask(int subtaskId) {
        int indexToRemove = -1;
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).id == subtaskId) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0) {
            subtasks.remove(indexToRemove);
            verifyEpicStatus();
        }
    }

    public void removeAllSubtasks() {
        subtasks = new ArrayList<>();
        verifyEpicStatus();
    }

    @Override
    public void setStatus(TaskStatus status) throws EpicSetStatusException {
        if (this.status != status)
            throw new EpicSetStatusException(id);
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
}
