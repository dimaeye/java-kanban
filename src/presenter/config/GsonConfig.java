package presenter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GsonConfig {
    private static Gson gson;

    private GsonConfig() {
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
        }
        return gson;
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");


        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime != null)
                jsonWriter.value(localDateTime.format(formatter));
            else
                jsonWriter.value("");
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            String dt = jsonReader.nextString();
            if (dt.isBlank())
                return null;
            else
                return LocalDateTime.parse(dt, formatter);
        }
    }
}
