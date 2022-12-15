package domain;

public class EpicSetStatusException extends RuntimeException {
    public EpicSetStatusException(int id) {
        super("Изменение статуса эпика " + id + " не выполнена! Статус эпика зависит от его подзадач.");
    }
}
