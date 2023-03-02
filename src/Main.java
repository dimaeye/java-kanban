import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.TaskStatus;
import domain.exceptions.TaskNotFoundException;
import managers.Managers;
import managers.historymanager.HistoryManager;
import managers.historymanager.inmemory.InMemoryHistoryManagerImpl;
import managers.taskmanager.TaskManager;
import managers.taskmanager.infile.FileBackedTaskManagerImpl;
import presenter.server.HttpTaskServer;
import presenter.server.KVServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    private static final int DELIMITER_LINE_SIZE = 120;
    private static final String DELIMITER = "-";

    public static void main(String[] args) throws IOException {
        KVServer kvServer = new KVServer();
        kvServer.start();

        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            httpTaskServer.stop();
            kvServer.stop();
        }));

        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        createTasks();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        createEpics();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        /*changeTasksStatus();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        changeSubtasksStatusAndCheckEpicStatus();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        removeTask();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        removeSubtask();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        removeEpic();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        checkHistory();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        removeAll();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        checkHistoryOrder();
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        System.out.println("Проверка восстановления задач и истории просмотров из файла");
        assertEqualsManagers();*/
    }

    private static void createTasks() {
        System.out.println("Создаем 2 задачи");

        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(taskManager.getUniqueTaskId(), "Задача 1", "Описание задачи 1"));
        tasks.add(new Task(taskManager.getUniqueTaskId(), "Задача 2", "Описание задачи 2"));
        for (Task task : tasks)
            taskManager.createTask(task);

        List<Task> actualTasks = taskManager.getAllTasks();
        actualTasks.forEach(System.out::println);
    }

    private static void createEpics() {
        System.out.println("Создаем один эпик с 3 подзадачами, а другой эпик с 1 подзадачей");

        Epic firstEpic = new Epic(taskManager.getUniqueEpicId(), "Эпик 1", "Описание эпика 1");
        Epic secondEpic = new Epic(taskManager.getUniqueEpicId(), "Эпик 2", "Описание эпика 2");

        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 1 (Эпик 1)", "Описание подзадачи 1", firstEpic)
        );
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 2 (Эпик 1)", "Описание подзадачи 2", firstEpic)
        );
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 3 (Эпик 1)", "Описание подзадачи 3", firstEpic)
        );
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 1 (Эпик 2)", "Описание подзадачи 1", secondEpic)
        );

        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.updateEpic(
                new Epic(secondEpic.getId(), "Эпик 2 с новым названием", secondEpic.getDescription())
        );
        for (Subtask subtask : subtasks)
            taskManager.createSubtask(subtask);

        List<Epic> actualEpics = taskManager.getAllEpics();
        actualEpics.forEach(System.out::println);
        actualEpics.forEach(epic -> epic.getAllRelatedTasks().forEach(System.out::println));
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        List<Subtask> actualSubtasks = taskManager.getAllSubtasks();
        actualSubtasks.forEach(System.out::println);
    }

    private static void changeTasksStatus() {
        System.out.println("Изменяем статусы созданных задач");

        List<Task> tasks = taskManager.getAllTasks();
        for (Task task : tasks) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskManager.updateTask(task);
            System.out.println(taskManager.getTask(task.getId()));
        }
    }

    private static void changeSubtasksStatusAndCheckEpicStatus() {
        System.out.println("Изменяем статусы созданных подзадач и проверяем, "
                + "что статус эпика рассчитался по статусам подзадач");

        List<Subtask> subtasks = taskManager.getAllSubtasks();
        for (Subtask subtask : subtasks) {
            Epic relatedEpic = (Epic) subtask.getAllRelatedTasks().get(0);
            subtask.setStatus(TaskStatus.DONE);
            taskManager.updateSubtask(subtask);
            System.out.println(taskManager.getSubtask(subtask.getId()));
            System.out.println("Статус эпика " + relatedEpic.getId()
                    + " - " + taskManager.getEpic(relatedEpic.getId()).getStatus());
        }
    }

    private static void removeTask() {
        System.out.println("Удаляем одну из задач");

        List<Task> tasks = taskManager.getAllTasks();
        taskManager.removeTask(tasks.get(0).getId());
        taskManager.getAllTasks().forEach(System.out::println);
    }

    private static void removeSubtask() {
        System.out.println("Удаляем одну из подзадач");

        int subtaskIdForRemove = taskManager.getAllSubtasks().get(0).getId();

        System.out.println("Список подзадач до удаления:");
        taskManager.getAllSubtasks().forEach(System.out::println);

        System.out.println("Удаляем подзадачу с идентификатором " + subtaskIdForRemove);
        taskManager.removeSubtask(subtaskIdForRemove);

        System.out.println("Список подзадач после удаления:");
        taskManager.getAllSubtasks().forEach(System.out::println);
    }

    private static void removeEpic() {
        System.out.println("Удаляем один из эпиков и проверяем список всех подзадач");

        List<Epic> epics = taskManager.getAllEpics();
        int epicIdForRemove = epics.get(0).getId();
        taskManager.removeEpic(epicIdForRemove);
        taskManager.getAllEpics().forEach(System.out::println);

        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        try {
            taskManager.getAllSubtasksOfEpic(epicIdForRemove).forEach(System.out::println);
        } catch (TaskNotFoundException ex) {
            System.out.println("Подзадачи удаленного эпика не найдена");
            System.out.println(ex.getMessage());
        }
    }

    private static void checkHistory() {
        System.out.println("Текущая история просмотра задач:");
        historyManager.getHistory().forEach(System.out::println);
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));

        System.out.println("Проверка обновления журнала история");
        Task anyTask = taskManager.getAllTasks().stream().findAny().orElseThrow();
        Task expectedTask = taskManager.getTask(anyTask.getId());
        System.out.println("Выполнен просмотр задачи - " + expectedTask);

        List<Task> history = historyManager.getHistory();
        Task actualTask = history.get(history.size() - 1);
        if (expectedTask.equals(actualTask)) {
            System.out.println("Текущая история просмотра задач:");
            history.forEach(System.out::println);
            System.out.println("Просмотр последний задачи в менеджере истории отображается верно");
        } else
            System.out.println("Менеджер истории не отобразил последний просмотр задачи!");
    }

    private static void removeAll() {
        System.out.println("Список задач до удаления - " + taskManager.getAllTasks());
        taskManager.removeAllTasks();
        System.out.println("Список задач после удаления - " + taskManager.getAllTasks());

        System.out.println("Список подзадач до удаления - " + taskManager.getAllSubtasks());
        taskManager.removeAllSubtasks();
        System.out.println("Список подзадач после удаления - " + taskManager.getAllSubtasks());

        System.out.println("Список эпиков до удаления - " + taskManager.getAllEpics());
        taskManager.removeAllEpics();
        System.out.println("Список эпиков после удаления - " + taskManager.getAllEpics());
    }

    private static void checkHistoryOrder() {
        if (historyManager.getHistory().size() > 0)
            removeAll();
        List<Task> allTasks = new ArrayList<>();

        System.out.println("создаем две задачи");
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(taskManager.getUniqueTaskId(), "Задача 1", "Описание задачи 1"));
        tasks.add(new Task(taskManager.getUniqueTaskId(), "Задача 2", "Описание задачи 2"));
        allTasks.addAll(tasks);
        for (Task task : tasks)
            taskManager.createTask(task);

        System.out.println("создаем эпик с тремя подзадачами и эпик без подзадач;");
        Epic epicWithSubtasks = new Epic(taskManager.getUniqueEpicId(), "Эпик 1", "Описание эпика 1");
        Epic emptyEpic = new Epic(taskManager.getUniqueEpicId(), "Эпик 2", "Описание эпика 2");
        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 1 (Эпик 1)", "Описание подзадачи 1", epicWithSubtasks)
        );
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 2 (Эпик 1)", "Описание подзадачи 2", epicWithSubtasks)
        );
        subtasks.add(
                new Subtask(taskManager.getUniqueSubtaskId(),
                        "Подзадача 3 (Эпик 1)", "Описание подзадачи 3", epicWithSubtasks)
        );
        taskManager.createEpic(epicWithSubtasks);
        taskManager.createEpic(emptyEpic);
        allTasks.add(epicWithSubtasks);
        allTasks.add(emptyEpic);
        for (Subtask subtask : subtasks)
            taskManager.createSubtask(subtask);
        allTasks.addAll(subtasks);

        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        System.out.println("запросим созданные задачи несколько раз в разном порядке");
        int randomRepeatCount = getRandomNumber();
        System.out.println("Количество повторных вызовов: " + randomRepeatCount);
        for (int i = 0; i < randomRepeatCount; i++) {
            Task currentTask = allTasks.get(getRandomNumber(allTasks.size() - 1));
            System.out.println("Выполнен просмотр задачи: ");
            System.out.println(currentTask);
            System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
            if (currentTask instanceof Epic) {
                taskManager.getEpic(currentTask.getId());
                historyManager.getHistory().forEach(System.out::println);
                System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
            } else if (currentTask instanceof Subtask) {
                taskManager.getSubtask(currentTask.getId());
                historyManager.getHistory().forEach(System.out::println);
                System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
            } else {
                taskManager.getTask(currentTask.getId());
                historyManager.getHistory().forEach(System.out::println);
                System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
            }
            int lastIndex = historyManager.getHistory().size() - 1;
            if (historyManager.getHistory().get(lastIndex) != currentTask)
                throw new RuntimeException("Менеджер истории не отобразил последний просмотр задачи!");
            if (historyManager.getHistory().stream().map(Task::getId).distinct().count()
                    != historyManager.getHistory().size())
                throw new RuntimeException("Менеджер истории содержит дубликаты!");
        }

        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        System.out.println("Удалим задачу и проверим историю");
        for (Task task : tasks)
            taskManager.getTask(task.getId());
        historyManager.getHistory().forEach(System.out::println);
        taskManager.removeTask(tasks.get(0).getId());
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        historyManager.getHistory().forEach(System.out::println);

        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        System.out.println("Удалим эпик и проверим историю");
        for (Subtask subtask : subtasks)
            taskManager.getSubtask(subtask.getId());
        taskManager.getEpic(epicWithSubtasks.getId());
        taskManager.getEpic(emptyEpic.getId());
        historyManager.getHistory().forEach(System.out::println);
        System.out.println(DELIMITER.repeat(DELIMITER_LINE_SIZE));
        taskManager.removeEpic(epicWithSubtasks.getId());
        historyManager.getHistory().forEach(System.out::println);
    }

    private static void assertEqualsManagers() {
        HistoryManager newHistoryManager = new InMemoryHistoryManagerImpl();
        TaskManager newTaskManager = new FileBackedTaskManagerImpl(newHistoryManager, "/tmp/tasks.csv");

        if (taskManager.getAllTasks().equals(newTaskManager.getAllTasks()))
            System.out.println("Задачи из файла восстановлены верно");
        else
            throw new RuntimeException("Задачи из файла восстановлены некорректно!");

        if (taskManager.getAllEpics().equals(newTaskManager.getAllEpics()))
            System.out.println("Эпики из файла восстановлены верно");
        else
            throw new RuntimeException("Эпики из файла восстановлены некорректно!");

        if (taskManager.getAllSubtasks().equals(newTaskManager.getAllSubtasks()))
            System.out.println("Подзадачи из файла восстановлены верно");
        else
            throw new RuntimeException("Подзадачи из файла восстановлены некорректно!");

        if (historyManager.getHistory().equals(newHistoryManager.getHistory()))
            System.out.println("История просмотров восстановлена верно");
        else
            throw new RuntimeException("История просмотров восстановлена некорректно!");

    }

    private static int getRandomNumber() {
        return (int) ((Math.random() * (10 - 1)) + 5);
    }

    private static int getRandomNumber(int max) {
        return (int) (Math.random() * (max - 1));
    }
}
