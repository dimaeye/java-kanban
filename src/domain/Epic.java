package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subtasks;

    public Epic(Integer id, String title, String description, List<Subtask> subtasks) {
        super(id, title, description);
        this.subtasks = subtasks;
        verifyEpicStatus();
    }

    private void verifyEpicStatus() {
        if (subtasks.isEmpty()
                || subtasks.stream().filter(subtask -> subtask.status == TaskStatus.NEW).count() == subtasks.size())
            status = TaskStatus.NEW;
        else if (subtasks.stream().filter(subtask -> subtask.status == TaskStatus.DONE).count() == subtasks.size())
            status = TaskStatus.DONE;
        else
            status = TaskStatus.IN_PROGRESS;
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubtask(Subtask subtask) {
        if (Objects.equals(subtask.getEpicId(), id)) {
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
}
