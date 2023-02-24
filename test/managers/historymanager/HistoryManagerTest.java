package managers.historymanager;

import domain.Task;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HistoryManagerTest<T extends HistoryManager> {

    protected T historyManager;
    protected final EasyRandom generator = new EasyRandom();

    abstract protected void beforeEach();

    abstract protected void afterEach();

    @Test
    void shouldReturnEmptyHistoryAfterRemoveAllViews() {
        int tasksCount = 10;
        final List<Task> tasks = generator.objects(Task.class, tasksCount).collect(Collectors.toList());
        tasks.forEach(task -> historyManager.add(task));

        assertAll(
                () -> assertEquals(tasksCount, historyManager.getHistory().size()),
                () -> {
                    for (int i = 0; i < tasksCount; i++) {
                        historyManager.remove(tasks.get(i).getId());
                    }
                    assertTrue(historyManager.getHistory().isEmpty());
                }
        );
    }

    @Test
    void shouldReturnOnlyOneTaskInHistoryAfterManyViews() {
        int duplicateViews = 15;
        final Task task = generator.nextObject(Task.class);

        for (int i = 0; i < duplicateViews; i++) {
            historyManager.add(task);
        }

        assertAll(
                () -> assertEquals(1, historyManager.getHistory().size()),
                () -> assertEquals(task, historyManager.getHistory().get(0))
        );
    }

    @Test
    void checkRemoveViewFromTheTop() {
        int tasksCount = 10;
        final List<Task> tasks = generator.objects(Task.class, tasksCount).collect(Collectors.toList());
        tasks.forEach(task -> historyManager.add(task));

        historyManager.remove(tasks.get(0).getId());
        tasks.remove(0);

        assertEquals(tasks, historyManager.getHistory());
    }

    @Test
    void checkRemoveViewFromTheMiddle() {
        int tasksCount = 15;
        final List<Task> tasks = generator.objects(Task.class, tasksCount).collect(Collectors.toList());
        tasks.forEach(task -> historyManager.add(task));

        int indexToRemove = tasksCount / 2;
        historyManager.remove(tasks.get(indexToRemove).getId());
        tasks.remove(indexToRemove);

        assertEquals(tasks, historyManager.getHistory());
    }

    @Test
    void checkRemoveViewFromTheEnd() {
        int tasksCount = 10;
        final List<Task> tasks = generator.objects(Task.class, tasksCount).collect(Collectors.toList());
        tasks.forEach(task -> historyManager.add(task));

        historyManager.remove(tasks.get(tasksCount - 1).getId());
        tasks.remove(tasksCount - 1);

        assertEquals(tasks, historyManager.getHistory());
    }
}