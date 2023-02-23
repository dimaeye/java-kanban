package managers.taskmanager.inmemory;

import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManagerTest;
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

    public InMemoryTaskManagerImplTest() {
        super(new InMemoryTaskManagerImpl(historyManager));
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        taskManager = new InMemoryTaskManagerImpl(historyManager);
    }

    @Override
    protected void afterEach() {
    }
}