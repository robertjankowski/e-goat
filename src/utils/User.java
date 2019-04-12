package utils;

import java.net.InetAddress;

public class User {

    private String login;
    private InetAddress address;

    public User(String login, InetAddress address) {
        this.login = login;
        this.address = address;
    }

    @Override
    public String toString() {
        return login + " " + address.toString();
    }
}
