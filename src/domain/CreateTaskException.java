package domain;

public class CreateTaskException extends RuntimeException {
    public CreateTaskException(int id) {
        super("Задача с идентификатором " + id + " уже создана!");
    }
}
