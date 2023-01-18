package managers.historymanager.inmemory;

import domain.Task;

class TaskNode<T extends Task> {
    T task;
    TaskNode<T> next;
    TaskNode<T> prev;

    public TaskNode(T task, TaskNode<T> next, TaskNode<T> prev) {
        this.task = task;
        this.next = next;
        this.prev = prev;
    }
}
