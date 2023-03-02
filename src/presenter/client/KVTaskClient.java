package presenter.client;

public interface KVTaskClient {
    void put(String key, String json);

    String load(String key); // key будет идентификатор задачи //value будет значение json возможно в обернутом виде
}
