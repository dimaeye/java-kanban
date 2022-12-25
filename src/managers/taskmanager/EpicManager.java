package managers.taskmanager;

import domain.Subtask;
import domain.exceptions.TaskNotFoundException;

import java.util.List;

public interface EpicManager {
    List<Subtask> getAllSubtasksOfEpic(int epicId) throws TaskNotFoundException;
}
