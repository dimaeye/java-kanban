package taskmanager;

import domain.Subtask;
import domain.TaskNotFoundException;

import java.util.List;

public interface EpicManager {
    List<Subtask> getAllSubtasksOfEpic(Integer epicId) throws TaskNotFoundException;
}
