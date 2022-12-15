package taskmanager;

import domain.Epic;
import domain.Task;

import java.util.HashMap;
import java.util.Map;

final class DataStore {
    final static Map<Integer, Task> tasks = new HashMap<>();
    final static Map<Integer, Epic> epics = new HashMap<>();
}
