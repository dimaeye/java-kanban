package managers.taskmanager.inmemory;

import domain.Epic;
import domain.Task;

import java.util.HashMap;
import java.util.Map;

final class InMemoryDataStore {
    final static Map<Integer, Task> tasks = new HashMap<>();
    final static Map<Integer, Epic> epics = new HashMap<>();
}
