package managers.historymanager.inmemory;

import domain.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TaskLinkedList<T extends Task> {

    private final Map<Integer, TaskNode<T>> history = new HashMap<>();
    private TaskNode<T> first;
    private TaskNode<T> last;

    public void add(T task) {
        linkLast(task);
    }

    public void removeTask(int taskId) {
        if (history.containsKey(taskId)) {
            removeNode(history.get(taskId));
            history.remove(taskId);
        }
    }

    public List<T> getTasks() {
        List<T> tasks = new ArrayList<>(history.size());
        TaskNode<T> cur = first;
        while (cur != null) {
            tasks.add(cur.task);
            cur = cur.next;
        }
        return tasks;
    }

    private void linkLast(T task) {
        if (history.containsKey(task.getId()))
            removeNode(history.get(task.getId()));
        TaskNode<T> newNode;
        if (first == null) {
            newNode = new TaskNode<>(task, null, null);
            first = newNode;
        } else if (last == null) {
            newNode = new TaskNode<>(task, null, first);
            last = newNode;
            first.next = newNode;
        } else {
            TaskNode<T> currLast = last;
            newNode = new TaskNode<>(task, null, currLast);
            currLast.next = newNode;
            last = newNode;
        }
        history.put(task.getId(), newNode);
    }

    private void removeNode(TaskNode<T> node) {
        TaskNode<T> next = node.next;
        TaskNode<T> prev = node.prev;
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }
        history.remove(node.task.getId());
        node.task = null;
    }

    private static class TaskNode<T extends Task> {
        T task;
        TaskNode<T> next;
        TaskNode<T> prev;

        public TaskNode(T task, TaskNode<T> next, TaskNode<T> prev) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }
}
