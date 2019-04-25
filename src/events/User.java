package events;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private static final String SHARED_FOLDER = "src/client/shared";
    private String login;
    private InetAddress address;
    private int port;
    private List<String> files;

    public User(String login, InetAddress address, int port) {
        this.login = login;
        this.address = address;
        this.port = port;
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "LOGIN: " + login + " | ADDRESS: " +
                address.toString() + " | PORT: " + port;
    }

    public String showListOfFiles() {
        String user = login + "\t" + address.toString() + "\t" + port;
        String filesString = String.join("\n", files);
        return user + "\n" + filesString;
    }

    public static List<String> getFiles() {
        List<String> files = null;
        var path = FileSystems.getDefault().getPath(SHARED_FOLDER).toAbsolutePath();
        try (var dir = Files.list(path)) {
            files = dir
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public String getLogin() {
        return login;
    }

    public boolean same(User user) {
        return this.address == user.address && this.login.equals(user.login);
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
