package utils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private String login;
    private InetAddress address;
    private List<String> files;

    public User(String login, InetAddress address) {
        this.login = login;
        this.address = address;
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        String user = login + "\t" + address.toString();
        String filesString = String.join("\n", files);
        return user + "\n" + filesString;
    }

    public static List<String> getFiles() {
        List<String> files = null;
        var path = FileSystems.getDefault().getPath(Config.SHARED_FOLDER).toAbsolutePath();
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
