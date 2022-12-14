package domain;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(int id) {
        super("Задача с идентификатором " + id + " не найдена!");
    }
}
