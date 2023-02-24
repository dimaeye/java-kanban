package managers.historymanager.inmemory;

import managers.historymanager.HistoryManagerTest;
import org.junit.jupiter.api.BeforeEach;

class InMemoryHistoryManagerImplTest extends HistoryManagerTest<InMemoryHistoryManagerImpl> {

    @BeforeEach
    @Override
    protected void beforeEach() {
        historyManager = new InMemoryHistoryManagerImpl();
    }

    @Override
    protected void afterEach() {
    }
}