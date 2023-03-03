package presenter.server.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import domain.Epic;
import domain.Subtask;
import domain.Task;
import domain.exceptions.CreateTaskException;
import domain.exceptions.OverlappingTaskTimeException;
import domain.exceptions.TaskNotFoundException;
import managers.Managers;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import presenter.config.GsonConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

public class TasksHandler implements HttpHandler {
    private final Gson gson = GsonConfig.getGson();
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final TaskManager taskManager = Managers.getDefault();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Map<RequestInfo, BiConsumer<RequestInfo, HttpExchange>> taskHandlers = new HashMap<>();

    public TasksHandler() {
        initHttpHandlers();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String method = exchange.getRequestMethod();
        RequestInfo requestInfo = new RequestInfo(uri, method);
        taskHandlers.get(requestInfo).accept(requestInfo, exchange);
    }

    private void initHttpHandlers() {
        //task routes
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task"), "GET"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    getAllTasks(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task?id=*"), "GET"), this::getTask);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task"), "POST"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    createTask(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task?id=*"), "DELETE"), this::deleteTask);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task"), "DELETE"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    deleteAllTasks(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/task?id=*"), "PUT"), this::updateTask);

        //epic routes
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic"), "GET"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    getAllEpics(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic?id=*"), "GET"), this::getEpic);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic"), "POST"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    createEpic(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic?id=*"), "DELETE"), this::deleteEpic);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic"), "DELETE"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    deleteAllEpics(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/epic?id=*"), "PUT"), this::updateEpic);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask/epic?id=*"), "GET"),
                this::getAllSubtasksOfEpic);

        //subtask route
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask"), "GET"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    getAllSubtasks(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask?id=*"), "GET"), this::getSubtask);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask/epic?id=*"), "POST"), this::createSubtask);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask?id=*"), "DELETE"), this::deleteSubtask);
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask"), "DELETE"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    deleteAllSubtasks(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks/subtask?id=*"), "PUT"), this::updateSubtask);

        //other route
        taskHandlers.put(new RequestInfo(URI.create("/tasks/history"), "GET"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    getHistory(exchange);
                }
        );
        taskHandlers.put(new RequestInfo(URI.create("/tasks"), "GET"),
                (RequestInfo requestInfo, HttpExchange exchange) -> {
                    getPrioritizedTasks(exchange);
                }
        );
    }

    private void getAllTasks(HttpExchange exchange) {
        try {
            List<Task> tasks = taskManager.getAllTasks();

            writeJsonBody(gson.toJson(tasks), exchange);
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getTask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int taskId = Integer.parseInt(requestInfo.paramValues.get("id"));
            Task task = taskManager.getTask(taskId);

            writeJsonBody(gson.toJson(task), exchange);
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void createTask(HttpExchange exchange) {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            task.setId(taskManager.getUniqueTaskId());

            taskManager.createTask(task);

            exchange.sendResponseHeaders(201, 0);
            exchange.close();
        } catch (IOException | CreateTaskException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteTask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int taskId = Integer.parseInt(requestInfo.paramValues.get("id"));

            taskManager.removeTask(taskId);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteAllTasks(HttpExchange exchange) {
        try {
            taskManager.removeAllTasks();

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void updateTask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int taskId = Integer.parseInt(requestInfo.paramValues.get("id"));
            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

            Task task = gson.fromJson(body, Task.class);
            task.setId(taskId);

            taskManager.updateTask(task);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getAllEpics(HttpExchange exchange) {
        try {
            List<Epic> tasks = taskManager.getAllEpics();

            writeJsonBody(gson.toJson(tasks), exchange);
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getEpic(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int epicId = Integer.parseInt(requestInfo.paramValues.get("id"));
            Epic epic = taskManager.getEpic(epicId);

            writeJsonBody(gson.toJson(epic), exchange);
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void createEpic(HttpExchange exchange) {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

            Epic epic = gson.fromJson(body, Epic.class);
            epic.setId(taskManager.getUniqueEpicId());
            epic.getAllRelatedTasks().forEach(subtask -> {
                subtask.setId(taskManager.getUniqueSubtaskId());
                subtask.addRelatedTask(epic);
            });

            taskManager.createEpic(epic);

            exchange.sendResponseHeaders(201, 0);
            exchange.close();
        } catch (IOException | CreateTaskException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteEpic(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int epicId = Integer.parseInt(requestInfo.paramValues.get("id"));

            taskManager.removeEpic(epicId);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteAllEpics(HttpExchange exchange) {
        try {
            taskManager.removeAllEpics();

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void updateEpic(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int epicId = Integer.parseInt(requestInfo.paramValues.get("id"));
            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

            Epic epic = gson.fromJson(body, Epic.class);
            epic.setId(epicId);

            taskManager.updateEpic(epic);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getAllSubtasksOfEpic(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int epicId = Integer.parseInt(requestInfo.paramValues.get("id"));
            List<Subtask> subtasks = taskManager.getAllSubtasksOfEpic(epicId);

            writeJsonBody(gson.toJson(subtasks), exchange);
        } catch (IOException | TaskNotFoundException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getAllSubtasks(HttpExchange exchange) {
        try {
            List<Subtask> subtasks = taskManager.getAllSubtasks();

            writeJsonBody(gson.toJson(subtasks), exchange);
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getSubtask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int subtaskId = Integer.parseInt(requestInfo.paramValues.get("id"));
            Subtask subtask = taskManager.getSubtask(subtaskId);

            writeJsonBody(gson.toJson(subtask), exchange);
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void createSubtask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int epicId = Integer.parseInt(requestInfo.paramValues.get("id"));
            Epic epic = taskManager.getEpic(epicId);

            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            subtask.setId(taskManager.getUniqueSubtaskId());
            subtask.addRelatedTask(epic);

            taskManager.createSubtask(subtask);

            exchange.sendResponseHeaders(201, 0);
            exchange.close();
        } catch (IOException | CreateTaskException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteSubtask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int subtaskId = Integer.parseInt(requestInfo.paramValues.get("id"));

            taskManager.removeSubtask(subtaskId);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void deleteAllSubtasks(HttpExchange exchange) {
        try {
            taskManager.removeAllSubtasks();

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void updateSubtask(RequestInfo requestInfo, HttpExchange exchange) {
        try {
            int subtaskId = Integer.parseInt(requestInfo.paramValues.get("id"));
            String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

            Subtask subtask = gson.fromJson(body, Subtask.class);
            subtask.setId(subtaskId);

            taskManager.updateSubtask(subtask);

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        } catch (IOException | TaskNotFoundException | OverlappingTaskTimeException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getPrioritizedTasks(HttpExchange exchange) {
        try {
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

            writeJsonBody(gson.toJson(prioritizedTasks), exchange);
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void getHistory(HttpExchange exchange) {
        try {
            List<Task> history = historyManager.getHistory();

            writeJsonBody(gson.toJson(history), exchange);
        } catch (IOException e) {
            e.printStackTrace();
            //add error handler
        }
    }

    private void writeJsonBody(String body, HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders()
                .add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(DEFAULT_CHARSET));
        }
    }

    private static class RequestInfo {
        private final URI uri;
        private final String path;
        private final String method;
        private String entityName;
        private final Set<String> params = new HashSet<>();
        private final Map<String, String> paramValues = new HashMap<>();

        RequestInfo(URI uri, String method) {
            this.uri = uri;
            this.path = uri.getPath();
            this.method = method;
            parseUri();
        }

        public String getMethod() {
            return method;
        }

        public String getEntityName() {
            return entityName;
        }

        public Map<String, String> getParamValues() {
            return paramValues;
        }

        private void parseUri() {
            String[] splitPath = path.split("/");
            entityName = splitPath[1];

            String urn = uri.toString();
            if (urn.contains("?")) {
                String[] splitParams = urn.split("\\?")[1].split(",");
                for (int i = 0; i < splitParams.length; i++) {
                    String[] splitParam = splitParams[i].split("=");
                    params.add(splitParam[0]);
                    paramValues.put(splitParam[0], URLDecoder.decode(splitParam[1], DEFAULT_CHARSET));
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RequestInfo that = (RequestInfo) o;
            return path.equals(that.path) && method.equals(that.method)
                    && entityName.equals(that.entityName) && params.equals(that.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, method, entityName, params);
        }
    }

}
