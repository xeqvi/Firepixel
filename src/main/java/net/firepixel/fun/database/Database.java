package net.firepixel.fun.database;

import java.sql.Connection;

public interface Database {

    void connect();

    void disconnect();

    Connection getConnection();

    void createTable(String table);
}