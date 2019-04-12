package utils;

public enum ClientOptions {
    NONE(0),
    GET_LIST_OF_FILES(1),
    GET_FILE(2),
    EXIT(3);

    private int id;

    ClientOptions(int id) {
        this.id = id;
    }

    public static ClientOptions fromId(int id) {
        for (ClientOptions options : values()) {
            if (options.id == id) {
                return options;
            }
        }
        return null;
    }

}
