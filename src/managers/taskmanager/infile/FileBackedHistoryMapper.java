package managers.taskmanager.infile;

import managers.historymanager.HistoryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class FileBackedHistoryMapper {
    private static final char ARG_SEPARATOR = ',';

    private FileBackedHistoryMapper() {
    }

    static String historyToString(HistoryManager historyManager) {
        return historyManager.getHistory().stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(String.valueOf(ARG_SEPARATOR)));
    }

    static List<Integer> historyFromString(String line) {
        if (line.isBlank())
            return new ArrayList<>();
        else
            return Arrays
                    .stream(line.split(String.valueOf(ARG_SEPARATOR)))
                    .map(Integer::parseInt).collect(Collectors.toList());
    }
}
