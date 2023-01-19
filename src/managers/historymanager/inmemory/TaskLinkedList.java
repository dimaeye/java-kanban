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
        if (history.containsKey(task.getId()))
            removeNode(history.get(task.getId()));
        TaskNode<T> newNode;
        if (this.first == null) {
            newNode = new TaskNode<>(task, null, null);
            this.first = newNode;
        } else if (this.last == null) {
            newNode = new TaskNode<>(task, null, this.first);
            this.last = newNode;
            this.first.next = newNode;
        } else {
            TaskNode<T> currLast = this.last;
            newNode = new TaskNode<>(task, null, currLast);
            currLast.next = newNode;
            this.last = newNode;
        }
        history.put(task.getId(), newNode);
    }

    private void removeNode(TaskNode<T> node) {
        TaskNode<T> next = node.next;
        TaskNode<T> prev = node.prev;
        if (prev == null) {
            this.first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            this.last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }
        history.remove(node.task.getId());
        node.task = null;
    }
}
