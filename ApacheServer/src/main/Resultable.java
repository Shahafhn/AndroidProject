package main;

import java.sql.Connection;
import java.sql.ResultSet;

public interface Resultable {
    void handleResultSet(Connection connection,ResultSet resultSet);
}
