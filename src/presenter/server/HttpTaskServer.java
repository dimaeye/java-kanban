package presenter.server;

import com.sun.net.httpserver.HttpServer;
import presenter.server.handler.TasksHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;

    private HttpServer httpServer;

    public void start() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        if (httpServer != null)
            httpServer.stop(0);
        System.out.println("HttpServer успешно остановлен");
    }
}
