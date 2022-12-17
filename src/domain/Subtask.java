package domain;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(int id, String title, String description, Epic epic) {
        super(id, title, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public int getEpicId() {
        return epic.getId();
    }

    @Override
    public void setStatus(TaskStatus status) {
        if (this.status != status) {
            super.setStatus(status);
            epic.verifyEpicStatus();
        }
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + getEpicId() +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
