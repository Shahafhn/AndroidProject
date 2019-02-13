package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseManager {

    public static void exampleOfUpdate(){
        String userName = "eli";
        String password = "hanehmad";
        try(Connection conn = getConn()){
            try(PreparedStatement statement = conn.prepareStatement("UPDATE users_table SET password=? WHERE username=?")){
                statement.setString(1, password);
                statement.setString(2, userName);
                boolean success = statement.executeUpdate() == 1;
                System.out.println("success: " + success);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void exampleOfSelect(){
        List<User> users = new ArrayList<>();
        try(Connection conn = getConn()){
            try(PreparedStatement statement = conn.prepareStatement("SELECT username, password FROM users_table ORDER BY username ASC")){
                try(ResultSet resultSet = statement.executeQuery()){
                    while (resultSet.next()){
                        String userName = resultSet.getString(1);
                        String password = resultSet.getString(2);
                        User user = new User(userName, password);
                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(User user : users)
            System.out.println(user);
    }



    public static void exampleOfInsert(){
        String userName = "blah";
        String password = "40254rf23";
        try(Connection conn = getConn()){
            boolean userAlreadyExists = false;
            try(PreparedStatement statement = conn.prepareStatement("select * FROM users_table WHERE username=?")){
                statement.setString(1, userName);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next())
                        userAlreadyExists = true;
                }
            }
            if(userAlreadyExists)
                return;

            try(PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO users_table(username,password) VALUES (?,?)")){
                statement.setString(1, userName);
                statement.setString(2, password);
                int rowsAffected = statement.executeUpdate();
                System.out.println("rows affected: " + rowsAffected);
            }
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println("here we understand that this key already exists in the table");
            if(e.getMessage().contains("'PRIMARY'")){

            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static Connection getConn() throws SQLException {
        String connectionString = "jdbc:mysql://localhost:3306/project_db?useSSL=false";
        String user = "root";
        String password = "wangm262";
        return DriverManager.getConnection(connectionString, user, password);
    }

    public static class User{
        private String userName, password;

        public User(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return userName + " " + password;
        }
    }
}
