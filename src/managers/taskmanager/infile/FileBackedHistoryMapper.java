package managers.taskmanager.infile;

import managers.historymanager.HistoryManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedHistoryMapper {
    static String historyToString(HistoryManager historyManager) {
        return historyManager.getHistory().stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(","));
    }

    static List<Integer> historyFromString(String line) {
        return Arrays.stream(line.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }
}
