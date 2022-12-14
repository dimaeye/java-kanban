package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    public Epic(Integer id, String title, String description, TaskStatus status, List<Subtask> subtasks) {
        super(id, title, description, status);
        this.subtasks = subtasks;
    }

    private List<Subtask> subtasks;

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubtask(Subtask subtask) {
        if (Objects.equals(subtask.getEpicId(), id))
            subtasks.add(subtask);
    }

    public void removeSubtask(int subtaskId) {
        int indexToRemove = -1;
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).id == subtaskId) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove >= 0)
            subtasks.remove(indexToRemove);
    }

    public void removeAllSubtasks() {
        subtasks = new ArrayList<>();
    }
}
