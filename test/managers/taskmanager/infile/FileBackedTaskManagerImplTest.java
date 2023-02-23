package managers.taskmanager.infile;

import domain.Task;
import managers.historymanager.HistoryManager;
import managers.taskmanager.TaskManagerTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class FileBackedTaskManagerImplTest extends TaskManagerTest<FileBackedTaskManagerImpl> {
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

    private static String filePath;

    public FileBackedTaskManagerImplTest() {
        super(new FileBackedTaskManagerImpl(historyManager, filePath));
    }

    @BeforeAll
    static void beforeAll() {
        createTemporaryFile();
    }

    @BeforeEach
    @Override
    protected void beforeEach() {
        createTemporaryFile();
        taskManager = new FileBackedTaskManagerImpl(historyManager, filePath);
    }

    @Override
    protected void afterEach() {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTemporaryFile() {
        try {
            Path temp = Files.createTempFile("", ".tmp");
            filePath = temp.toString();
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write(FileBackedTaskMapper.HEADER_OF_FILE + "\n\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}