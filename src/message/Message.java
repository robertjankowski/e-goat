package message;

import user.User;

public class Message {

    public static final String LOGIN = "LOGIN";
    public static final String FILE_LIST = "FILE_LIST";
    public static final String DOWNLOAD = "DOWNLOAD";

    private String message;
    private User user;

    public Message(String message, User user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
