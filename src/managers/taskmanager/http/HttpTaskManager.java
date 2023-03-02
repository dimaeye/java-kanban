package managers.taskmanager.http;

import managers.historymanager.HistoryManager;
import managers.taskmanager.infile.FileBackedTaskManagerImpl;

public class HttpTaskManager extends FileBackedTaskManagerImpl {
    public HttpTaskManager(HistoryManager historyManager, String path) {
        super(historyManager, path);
    }

    @Override
    protected void save() {
        super.save();
    }

    @Override
    protected void loadFromStorage() {
        super.loadFromStorage();
    }
}
