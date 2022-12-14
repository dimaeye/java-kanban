package domain;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(Integer id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }
}
