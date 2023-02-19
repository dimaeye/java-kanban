package domain.exceptions;

public class OverlappingTaskTimeException extends RuntimeException {
    public OverlappingTaskTimeException(int id) {
        super("Задача с идентификатором " + id + " пересекается по времени выполнения!");
    }
}
