import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskStatus;
import taskmanager.EpicManagerImpl;
import taskmanager.SubtaskManagerImpl;
import taskmanager.TaskManager;
import taskmanager.TaskManagerImpl;

import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
     * Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
     * Измените статусы созданных объектов, распечатайте.
     * Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
     * И, наконец, попробуйте удалить одну из задач и один из эпиков.
     */
    private static final TaskManager<Task> taskManager = new TaskManagerImpl();
    private static final TaskManager<Epic> epicManager = new EpicManagerImpl();
    private static final TaskManager<Subtask> subtaskManager = new SubtaskManagerImpl();

    public static void main(String[] args) {
        createTasks();
        System.out.println("-".repeat(120));
        createEpics();
        System.out.println("-".repeat(120));
        changeTasksStatus();
        System.out.println("-".repeat(120));
        changeSubtasksStatusAndCheckEpicStatus();
        System.out.println("-".repeat(120));
        removeTask();
        System.out.println("-".repeat(120));
        removeEpic();
    }

    private static void createTasks() {
        System.out.println("Создаем 2 задачи");
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(taskManager.getUniqueId(), "Задача 1", "Описание задачи 1"));
        tasks.add(new Task(taskManager.getUniqueId(), "Задача 2", "Описание задачи 2"));
        for (Task task : tasks)
            taskManager.create(task);

        List<Task> actualTasks = taskManager.getAll();
        actualTasks.forEach(System.out::println);
    }

    private static void createEpics() {
        System.out.println("Создаем один эпик с 2 подзадачами, а другой эпик с 1 подзадачей");
        Epic firstEpic = new Epic(epicManager.getUniqueId(), "Эпик 1", "Описание эпика 1");
        Epic secondEpic = new Epic(epicManager.getUniqueId(), "Эпик 2", "Описание эпика 2");


        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(
                new Subtask(subtaskManager.getUniqueId(),
                        "Подзадача 1 (Эпик 1)", "Описание подзадачи 1", firstEpic.getId())
        );
        subtasks.add(
                new Subtask(subtaskManager.getUniqueId(),
                        "Подзадача 2 (Эпик 1)", "Описание подзадачи 2", firstEpic.getId())
        );
        subtasks.add(
                new Subtask(subtaskManager.getUniqueId(),
                        "Подзадача 1 (Эпик 2)", "Описание подзадачи 1", secondEpic.getId())
        );

        epicManager.create(firstEpic);
        epicManager.create(secondEpic);
        for (Subtask subtask : subtasks)
            subtaskManager.create(subtask);

        List<Epic> actualEpics = epicManager.getAll();
        actualEpics.forEach(System.out::println);
        actualEpics.forEach(epic -> epic.getAllSubtasks().forEach(System.out::println));
        System.out.println("-".repeat(120));

        List<Subtask> actualSubtasks = subtaskManager.getAll();
        actualSubtasks.forEach(System.out::println);
    }

    private static void changeTasksStatus() {
        System.out.println("Изменяем статусы созданных задач");
        List<Task> tasks = taskManager.getAll();
        for (Task task : tasks) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskManager.update(task);
            System.out.println(taskManager.get(task.getId()));
        }
    }

    private static void changeSubtasksStatusAndCheckEpicStatus() {
        System.out.println("Изменяем статусы созданных подзадач и проверяем, "
                + "что статус эпика рассчитался по статусам подзадач");
        List<Subtask> subtasks = subtaskManager.getAll();
        for (Subtask subtask : subtasks) {
            subtask.setStatus(TaskStatus.DONE);
            subtaskManager.update(subtask);
            System.out.println(subtaskManager.get(subtask.getId()));
            System.out.println("Статус эпика " + subtask.getEpicId()
                    + " - " + epicManager.get(subtask.getEpicId()).getStatus());
        }
    }

    private static void removeTask() {
        System.out.println("Удаляем одну из задач");
        List<Task> tasks = taskManager.getAll();
        taskManager.remove(tasks.get(0).getId());
        taskManager.getAll().forEach(System.out::println);
    }

    private static void removeEpic() {
        System.out.println("Удаляем один из эпиков и проверяем список всех подзадач");
        List<Epic> epics = epicManager.getAll();
        epicManager.remove(epics.get(0).getId());
        epicManager.getAll().forEach(System.out::println);
        System.out.println("-".repeat(120));
        subtaskManager.getAll().forEach(System.out::println);
    }
}
