package user;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class User {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());
    private String login;
    private InetAddress address;
    private String randomPort1;
    private String randomPort2;
    private List<String> files;

    public User(InetAddress address) {
        this.address = address;
    }

    public User() {
        this.login = "";
    }

    @Override
    public String toString() {
        return "LOGIN: " + login + " | ADDRESS: " +
                address.toString() + " | PORT: " + randomPort1;
    }

    public String showListOfFiles() {
        String filesString = String.join(" \n", files);
        return "\tLOGIN: " + login + "\n\tFILES:\n" + filesString;
    }

    public static List<String> getFiles(String pathToFiles) {
        var path = FileSystems.getDefault()
                .getPath(pathToFiles)
                .toAbsolutePath();
        try (var dir = Files.list(path)) {
            return dir
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.severe("Wrong path to files!");
        }
        return null;
    }

    public static String listOfFilesToString(List<String> files) {
        return String.join(",", files);
    }

    public static List<String> listOfFileToList(String files) {
        return Arrays.asList(files.split(","));
    }

    public boolean sameUser(User user) {
        return Objects.equals(this.randomPort1, user.randomPort1) && Objects.equals(this.randomPort2, user.randomPort2);
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

    public void setListOfFiles(List<String> files) {
        this.files = files;
    }

    public String getLogin() {
        return login;
    }
}
