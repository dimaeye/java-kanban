package managers.taskmanager.http;

import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManager;
import managers.taskmanager.infile.FileBackedTaskManagerImplTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import presenter.server.HttpTaskServer;
import presenter.server.KVServer;

import java.io.IOException;

class HttpTaskManagerTest extends FileBackedTaskManagerImplTest {

    private HttpTaskServer httpTaskServer;
    private KVServer kvServer;

    @BeforeAll
    static void beforeAll() {
        path = "http://localhost:" + KVServer.PORT;
    }

    @BeforeEach
    @Override
    protected void beforeEach() throws IOException {
        kvServer = new KVServer();
        httpTaskServer = new HttpTaskServer();

        kvServer.start();
        httpTaskServer.start();

        historyManager = getStubHistoryManager();
        taskManager = new HttpTaskManager(historyManager, path);
    }

    @AfterEach
    @Override
    protected void afterEach() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Override
    protected TaskManager restoreTaskManager(HistoryManager historyManager, String path) {
        return new HttpTaskManager(historyManager, path);
    }


}