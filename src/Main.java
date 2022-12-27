import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskStatus;
import domain.exceptions.TaskNotFoundException;
import managers.Managers;
import managers.historymanager.HistoryManager;
import managers.taskmanager.GeneralTaskManager;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final GeneralTaskManager generalTaskManager = Managers.getDefault();
    private static final HistoryManager historyManager = Managers.getDefaultHistory();

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
        System.out.println("-".repeat(120));

        checkHistory();
        System.out.println("-".repeat(120));

        removeAll();
    }

    private static void createTasks() {
        System.out.println("Создаем 2 задачи");

        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(generalTaskManager.getUniqueTaskId(), "Задача 1", "Описание задачи 1"));
        tasks.add(new Task(generalTaskManager.getUniqueTaskId(), "Задача 2", "Описание задачи 2"));
        for (Task task : tasks)
            generalTaskManager.createTask(task);

        List<Task> actualTasks = generalTaskManager.getAllTasks();
        actualTasks.forEach(System.out::println);
    }

    private static void createEpics() {
        System.out.println("Создаем один эпик с 2 подзадачами, а другой эпик с 1 подзадачей");

        Epic firstEpic = new Epic(generalTaskManager.getUniqueEpicId(), "Эпик 1", "Описание эпика 1");
        Epic secondEpic = new Epic(generalTaskManager.getUniqueEpicId(), "Эпик 2", "Описание эпика 2");

        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(
                new Subtask(generalTaskManager.getUniqueSubtaskId(),
                        "Подзадача 1 (Эпик 1)", "Описание подзадачи 1", firstEpic)
        );
        subtasks.add(
                new Subtask(generalTaskManager.getUniqueSubtaskId(),
                        "Подзадача 2 (Эпик 1)", "Описание подзадачи 2", firstEpic)
        );
        subtasks.add(
                new Subtask(generalTaskManager.getUniqueSubtaskId(),
                        "Подзадача 1 (Эпик 2)", "Описание подзадачи 1", secondEpic)
        );

        generalTaskManager.createEpic(firstEpic);
        generalTaskManager.createEpic(secondEpic);
        generalTaskManager.updateEpic(
                new Epic(secondEpic.getId(), "Эпик 2 с новым названием", secondEpic.getDescription())
        );
        for (Subtask subtask : subtasks)
            generalTaskManager.createSubtask(subtask);

        List<Epic> actualEpics = generalTaskManager.getAllEpics();
        actualEpics.forEach(System.out::println);
        actualEpics.forEach(epic -> epic.getAllSubtasks().forEach(System.out::println));
        System.out.println("-".repeat(120));

        List<Subtask> actualSubtasks = generalTaskManager.getAllSubtasks();
        actualSubtasks.forEach(System.out::println);
    }

    private static void changeTasksStatus() {
        System.out.println("Изменяем статусы созданных задач");

        List<Task> tasks = generalTaskManager.getAllTasks();
        for (Task task : tasks) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            generalTaskManager.updateTask(task);
            System.out.println(generalTaskManager.getTask(task.getId()));
        }
    }

    private static void changeSubtasksStatusAndCheckEpicStatus() {
        System.out.println("Изменяем статусы созданных подзадач и проверяем, "
                + "что статус эпика рассчитался по статусам подзадач");

        List<Subtask> subtasks = generalTaskManager.getAllSubtasks();
        for (Subtask subtask : subtasks) {
            subtask.setStatus(TaskStatus.DONE);
            generalTaskManager.updateSubtask(subtask);
            System.out.println(generalTaskManager.getSubtask(subtask.getId()));
            System.out.println("Статус эпика " + subtask.getEpicId()
                    + " - " + generalTaskManager.getEpic(subtask.getEpicId()).getStatus());
        }
    }

    private static void removeTask() {
        System.out.println("Удаляем одну из задач");

        List<Task> tasks = generalTaskManager.getAllTasks();
        generalTaskManager.removeTask(tasks.get(0).getId());
        generalTaskManager.getAllTasks().forEach(System.out::println);
    }

    private static void removeEpic() {
        System.out.println("Удаляем один из эпиков и проверяем список всех подзадач");

        List<Epic> epics = generalTaskManager.getAllEpics();
        int epicIdForRemove = epics.get(0).getId();
        generalTaskManager.removeEpic(epicIdForRemove);
        generalTaskManager.getAllEpics().forEach(System.out::println);

        System.out.println("-".repeat(120));
        try {
            generalTaskManager.getAllSubtasksOfEpic(epicIdForRemove).forEach(System.out::println);
        } catch (TaskNotFoundException ex) {
            System.out.println("Подзадачи удаленного эпика не найдена");
            System.out.println(ex.getMessage());
        }

    }

    private static void checkHistory() {
        System.out.println("Текущая история просмотра задач:");
        historyManager.getHistory().forEach(System.out::println);
        System.out.println("-".repeat(120));

        System.out.println("Проверка обновления журнала история");
        Task anyTask = generalTaskManager.getAllTasks().stream().findAny().orElseThrow();
        Task expectedTask = generalTaskManager.getTask(anyTask.getId());
        System.out.println("Выполнен просмотр задачи - " + expectedTask);

        List<Task> history = historyManager.getHistory();
        Task actualTask = history.get(history.size() - 1);
        if (expectedTask.equals(actualTask)) {
            System.out.println("Текущая история просмотра задач:");
            history.forEach(System.out::println);
            System.out.println("Просмотр последний задачи в менеджере истории отображается верно");
        } else
            System.out.println("Менеджер истории не отобразил полседний просмотр задачи!");
    }

    private static void removeAll() {
        System.out.println("Список задач до удаления - " + generalTaskManager.getAllTasks());
        generalTaskManager.removeAllTasks();
        System.out.println("Список задач после удаления - " + generalTaskManager.getAllTasks());

        System.out.println("Список подзадач до удаления - " + generalTaskManager.getAllSubtasks());
        generalTaskManager.removeAllSubtasks();
        System.out.println("Список подзадач после удаления - " + generalTaskManager.getAllSubtasks());

        System.out.println("Список эпиков до удаления - " + generalTaskManager.getAllEpics());
        generalTaskManager.removeAllEpics();
        System.out.println("Список эпиков после удаления - " + generalTaskManager.getAllEpics());
    }
}
