package managers.taskmanager.inmemory;

import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManagerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerImplTest extends TaskManagerTest<InMemoryTaskManagerImpl> {

    private static final HistoryManager historyManager = new HistoryManager() {
        @Override
        public void add(Task task) {
        }

        @Override
        public void remove(int id) {
        }

        @Override
        public List<Task> getHistory() {
            return new ArrayList<>();
        }
    };

    @BeforeEach
    @Override
    protected void beforeEach() {
        taskManager = new InMemoryTaskManagerImpl(historyManager);
    }

    @AfterEach
    @Override
    protected void afterEach() {
    }
}