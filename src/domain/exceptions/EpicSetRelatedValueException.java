package domain.exceptions;

public class EpicSetRelatedValueException extends RuntimeException {
    public EpicSetRelatedValueException(int id, String fieldName) {
        super("Изменение " + fieldName + " эпика " + id + " не выполнена! " + fieldName
                + " эпика зависит от его подзадач.");
    }
}
