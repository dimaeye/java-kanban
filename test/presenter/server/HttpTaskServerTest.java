package presenter.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.Epic;
import domain.Subtask;
import domain.Task;
import managers.Managers;
import managers.taskmanager.TaskManager;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.*;
import presenter.config.GsonConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerTest {
    private static final String BASE_URL = "http://localhost:" + HttpTaskServer.PORT;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static HttpTaskServer httpTaskServer;
    private static KVServer kvServer;
    private TaskManager taskManager;

    private final Gson gson = GsonConfig.getGson();
    private final Type tasksTypeToken = new TypeToken<List<Task>>() {
    }.getType();
    private final Type epicsTypeToken = new TypeToken<List<Epic>>() {
    }.getType();
    private final Type subtasksTypeToken = new TypeToken<List<Subtask>>() {
    }.getType();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final EasyRandom generator = new EasyRandom();

    private List<Task> initialTasks = new ArrayList<>();
    private List<Epic> initialEpics = new ArrayList<>();
    private List<Subtask> initialSubtasks = new ArrayList<>();

    @BeforeAll
    static void setUp() throws IOException {
        kvServer = new KVServer();
        kvServer.start();

        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        initialTasks = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            initialTasks.add(
                    new Task(taskManager.getUniqueTaskId(),
                            generator.nextObject(String.class), generator.nextObject(String.class))
            );
        }
        initialTasks.sort(Comparator.comparingInt(Task::getId));


        initialEpics = new ArrayList<>();
        initialEpics.add(
                new Epic(taskManager.getUniqueEpicId(),
                        generator.nextObject(String.class), generator.nextObject(String.class))
        );
        initialEpics.add(
                new Epic(taskManager.getUniqueEpicId(),
                        generator.nextObject(String.class), generator.nextObject(String.class))
        );
        initialEpics.add(
                new Epic(taskManager.getUniqueEpicId(),
                        generator.nextObject(String.class), generator.nextObject(String.class))
        );
        initialEpics.sort(Comparator.comparingInt(Epic::getId));

        initialSubtasks = new ArrayList<>();
        for (Epic epic : initialEpics) {
            for (int i = 0; i < 10; i++) {
                Subtask subtask = new Subtask(
                        taskManager.getUniqueSubtaskId(),
                        generator.nextObject(String.class), generator.nextObject(String.class)
                );
                subtask.addRelatedTask(epic);
                initialSubtasks.add(subtask);
            }
        }
        initialSubtasks.sort(Comparator.comparingInt(Subtask::getId));


        initialTasks.forEach(taskManager::createTask);
        initialEpics.forEach(taskManager::createEpic);
    }

    @AfterEach
    void afterEach() {
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
    }

    @AfterAll
    static void tearDown() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    void getAllTasks() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(BASE_URL + "/tasks/task"))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    List<Task> actualTasks = gson.fromJson(httpResponse.body(), tasksTypeToken);
                    actualTasks.sort(Comparator.comparingInt(Task::getId));
                    assertEquals(initialTasks, actualTasks);
                }
        );
    }

    @Test
    void getTask() throws IOException, InterruptedException {
        Task expectedTask = initialTasks.get(getRandomNumberUsingNextInt(0, initialTasks.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/task?id=" + expectedTask.getId()))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    Task actualTask = gson.fromJson(httpResponse.body(), Task.class);
                    assertEquals(expectedTask, actualTask);
                }
        );
    }

    @Test
    void createTask() throws IOException, InterruptedException {
        Task newTask = generator.nextObject(Task.class);

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/task"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newTask), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(201, httpResponse.statusCode());
    }

    @Test
    void deleteTask() throws IOException, InterruptedException {
        Task taskForRemove = initialTasks.get(getRandomNumberUsingNextInt(0, initialTasks.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/task?id=" + taskForRemove.getId()))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void deleteAllTasks() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/task"))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void updateTask() throws IOException, InterruptedException {
        Task taskForUpdate = initialTasks.get(getRandomNumberUsingNextInt(0, initialTasks.size()));

        taskForUpdate.setTitle(generator.nextObject(String.class));
        taskForUpdate.setDescription(generator.nextObject(String.class));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/task?id=" + taskForUpdate.getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(taskForUpdate), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void getAllEpics() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(BASE_URL + "/tasks/epic"))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    List<Epic> actualEpics = gson.fromJson(httpResponse.body(), epicsTypeToken);
                    actualEpics.sort(Comparator.comparingInt(Task::getId));
                    for (Epic epic : actualEpics) {
                        epic.getAllRelatedTasks().forEach(subtask -> subtask.addRelatedTask(epic));
                    }
                    assertEquals(initialEpics, actualEpics);
                }
        );
    }

    @Test
    void getEpic() throws IOException, InterruptedException {
        Epic expectedEpic = initialEpics.get(getRandomNumberUsingNextInt(0, initialEpics.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/epic?id=" + expectedEpic.getId()))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    Epic actualEpic = gson.fromJson(httpResponse.body(), Epic.class);
                    actualEpic.getAllRelatedTasks().forEach(subtask -> subtask.addRelatedTask(expectedEpic));
                    assertEquals(expectedEpic, actualEpic);
                }
        );
    }

    @Test
    void createEpic() throws IOException, InterruptedException {
        Epic newEpic = new Epic(
                generator.nextInt(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/epic"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newEpic), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(201, httpResponse.statusCode());
    }

    @Test
    void deleteEpic() throws IOException, InterruptedException {
        Epic epicForRemove = initialEpics.get(getRandomNumberUsingNextInt(0, initialEpics.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/epic?id=" + epicForRemove.getId()))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void deleteAllEpics() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/epic"))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void updateEpic() throws IOException, InterruptedException {
        Epic epicForUpdate = initialEpics.get(getRandomNumberUsingNextInt(0, initialEpics.size()));

        epicForUpdate.setTitle(generator.nextObject(String.class));
        epicForUpdate.setDescription(generator.nextObject(String.class));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/epic?id=" + epicForUpdate.getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(epicForUpdate), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void getAllSubtasksOfEpic() throws IOException, InterruptedException {
        Epic epic = initialEpics.get(getRandomNumberUsingNextInt(0, initialEpics.size()));
        List<Subtask> expectedSubtasks = (List<Subtask>) (List<?>) epic.getAllRelatedTasks();
        expectedSubtasks.sort(Comparator.comparingInt(Subtask::getId));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask/epic?id=" + epic.getId()))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    List<Subtask> actualSubtasks = gson.fromJson(httpResponse.body(), subtasksTypeToken);
                    actualSubtasks.forEach(subtask -> subtask.addRelatedTask(epic));
                    actualSubtasks.sort(Comparator.comparingInt(Subtask::getId));

                    assertEquals(expectedSubtasks, actualSubtasks);
                }
        );
    }

    @Test
    void getAllSubtasks() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(BASE_URL + "/tasks/subtask"))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    List<Subtask> actualSubtasks = gson.fromJson(httpResponse.body(), subtasksTypeToken);
                    actualSubtasks.sort(Comparator.comparingInt(Task::getId));
                    actualSubtasks.forEach(subtask -> {
                        Epic epic = initialEpics.stream().filter(e -> e.getId() == subtask.getEpicId()).findFirst().get();
                        subtask.addRelatedTask(epic);
                    });

                    assertEquals(initialSubtasks, actualSubtasks);
                }
        );
    }

    @Test
    void getSubtask() throws IOException, InterruptedException {
        Subtask expectedSubtask = initialSubtasks.get(getRandomNumberUsingNextInt(0, initialSubtasks.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask?id=" + expectedSubtask.getId()))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    Subtask actualSubtask = gson.fromJson(httpResponse.body(), Subtask.class);
                    Epic epic = initialEpics.stream().filter(
                            e -> e.getId() == actualSubtask.getEpicId()
                    ).findFirst().get();
                    actualSubtask.addRelatedTask(epic);

                    assertEquals(expectedSubtask, actualSubtask);
                }
        );
    }

    @Test
    void createSubtask() throws IOException, InterruptedException {
        Epic epic = initialEpics.get(getRandomNumberUsingNextInt(0, initialEpics.size()));

        Subtask newSubtask = new Subtask(
                taskManager.getUniqueSubtaskId(), generator.nextObject(String.class), generator.nextObject(String.class)
        );

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask/epic?id=" + epic.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newSubtask), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(201, httpResponse.statusCode());
    }

    @Test
    void deleteSubtask() throws IOException, InterruptedException {
        Subtask subtaskForRemove = initialSubtasks.get(getRandomNumberUsingNextInt(0, initialSubtasks.size()));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask?id=" + subtaskForRemove.getId()))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void deleteAllSubtasks() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask"))
                .DELETE()
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void updateSubtask() throws IOException, InterruptedException {
        Subtask subtaskForUpdate = initialSubtasks.get(getRandomNumberUsingNextInt(0, initialSubtasks.size()));

        subtaskForUpdate.setTitle(generator.nextObject(String.class));
        subtaskForUpdate.setDescription(generator.nextObject(String.class));

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/subtask?id=" + subtaskForUpdate.getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(subtaskForUpdate), CHARSET))
                .build();
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        Subtask subtask = initialSubtasks.get(getRandomNumberUsingNextInt(0, initialSubtasks.size()));
        taskManager.getSubtask(subtask.getId());

        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks/history"))
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        assertAll(
                () -> assertEquals(200, httpResponse.statusCode()),
                () -> {
                    List<Task> history = gson.fromJson(httpResponse.body(), tasksTypeToken);
                    assertEquals(subtask.getId(), history.get(0).getId());
                }
        );
    }

    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(CHARSET));

        System.out.println(httpResponse.body());
        assertEquals(200, httpResponse.statusCode());
    }

    private int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

}