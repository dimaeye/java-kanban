package managers.historymanager.inmemory;

import domain.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TaskLinkedList<T extends Task> {

    private final Map<Integer, TaskNode<T>> history = new HashMap<>();
    private TaskNode<T> first;
    private TaskNode<T> last;

    public boolean add(T task) {
        linkLast(task);
        return true;
    }

    public T get(int id) {
        return history.get(id).task;
    }

    public boolean remove(T task) {
        if (history.containsKey(task.getId())) {
            removeNode(history.get(task.getId()));
            history.remove(task.getId());
        }
        return true;
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
        if (first == null)
            first = new TaskNode<>(task, null, null);
        else if (last != null) {
            TaskNode<T> newNode = new TaskNode<>(task, null, this.last);
            this.last.next = newNode;
            if (history.containsKey(task.getId()))
                removeNode(history.get(task.getId()));
            history.put(task.getId(), newNode);
        } else {
            TaskNode<T> newNode = new TaskNode<>(task, null, this.first);
            this.last = newNode;
            if (history.containsKey(task.getId()))
                removeNode(history.get(task.getId()));
            history.put(task.getId(), newNode);
        }
    }

    private void removeNode(TaskNode<T> node) {
        TaskNode<T> prev = node.prev;
        TaskNode<T> next = node.next;
        node.prev = null;
        node.next = null;
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        history.remove(node.task.getId());
    }
}
