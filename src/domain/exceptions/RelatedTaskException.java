package domain.exceptions;

public class RelatedTaskException extends RuntimeException {
    public RelatedTaskException() {
        super("Отсутствует возможность привязки задач!");
    }

    public RelatedTaskException(String message) {
        super(message);
    }
}
