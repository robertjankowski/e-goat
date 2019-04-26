package user;

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
    private String randomPort1;
    private String randomPort2;
    private List<String> files;

    public User(InetAddress address) {
        this.address = address;
    }

    public User(String login, InetAddress address, String randomPort1, String randomPort2) {
        this.login = login;
        this.address = address;
        this.randomPort1 = randomPort1;
        this.randomPort2 = randomPort2;
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "LOGIN: " + login + " | ADDRESS: " +
                address.toString() + " | PORT: " + randomPort1;
    }

    public String showListOfFiles() {
        String user = login + "\t" + address.toString() + "\t" + randomPort1;
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

    public InetAddress getAddress() {
        return address;
    }

    public void setRandomPort1(String randomPort1) {
        this.randomPort1 = randomPort1;
    }

    public void setRandomPort2(String randomPort2) {
        this.randomPort2 = randomPort2;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRandomPort1() {
        return randomPort1;
    }

    public String getRandomPort2() {
        return randomPort2;
    }
}
