package main;


import java.io.*;
import java.sql.*;
import javax.servlet.http.*;

public abstract class ActionsManager {

    public final static int SUCCESS = 200;
    public final static int FAILED = 300;
    public final static int USER_EXISTS = 401;
    public final static int USER_NOT_FOUND = 404;

    static void gotAction(Actions action, HttpServletRequest request, HttpServletResponse response){
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        switch (action) {
            case LOGIN:
                try {
                    makeStatement("SELECT * FROM project_gram WHERE username=? AND password=?",new String[]{user,pass}, (connection, resultSet) -> {
                        try {
                            response.setStatus(resultSet.next() ? SUCCESS : USER_NOT_FOUND);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            response.setStatus(USER_NOT_FOUND);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(USER_NOT_FOUND);
                }
                break;
            case REGISTER:
                try {
                    makeStatement("SELECT * FROM project_gram WHERE username=?", new String[]{user},(connection, resultSet) -> {
                        try {
                            if (!resultSet.next()){
                                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO project_gram (username,password) VALUES (?,?)")){
                                    statement.setString(1,user);
                                    statement.setString(2,pass);
                                    int rowsAffected = statement.executeUpdate();
                                    response.setStatus(rowsAffected != 0 ? SUCCESS : USER_EXISTS);
                                }
                            }else
                                response.setStatus(USER_EXISTS);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            response.setStatus(USER_EXISTS);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(USER_EXISTS);
                }
                break;
            case SET_PIC:
                try {
                    File file = FileHandler.saveImage(user,request);
                    if (file != null) {
                        makeStatement("SELECT * FROM project_gram WHERE username=?", new String[]{user},(connection, resultSet) -> {
                            try {
                                if (resultSet.next()){
                                    int images = resultSet.getInt(3);
                                    try (PreparedStatement statement = connection.prepareStatement("UPDATE users_table SET images=? WHERE username=?")){
                                        statement.setInt(1,images + 1);
                                        statement.setString(2,user);
                                        int rowsAffected = statement.executeUpdate();
                                        if (rowsAffected != 0)
                                            response.setStatus(SUCCESS);
                                        else {
                                            file.delete();
                                            response.setStatus(FAILED);
                                        }
                                    }
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    }else
                        response.setStatus(FAILED);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(FAILED);
                }
                break;
            case GET_PIC:
                boolean wasSent = FileHandler.sendImage(request,response,request.getParameter("ios") != null);
                response.setStatus(wasSent ? SUCCESS : FAILED);
                break;
            case GET_USERS:
                String look = request.getParameter("look");
                boolean ios = request.getParameter("ios") != null;
                try {
                    makeStatement("SELECT * FROM project_gram WHERE username LIKE ?", new String[]{"%" + look + "%"},(connection, resultSet) -> {
                        try (OutputStream outputStream = response.getOutputStream()){
                            while(resultSet.next()){
                                String username = resultSet.getString(1);
                                int images = resultSet.getInt(3);
                                if (!ios) {
                                    outputStream.write(username.length());
                                }
                                outputStream.write(username.getBytes());
                                if (ios){
                                    outputStream.write(92);
                                    outputStream.write(String.valueOf(images).getBytes());
                                    if (resultSet.next()){
                                        outputStream.write(124);
                                        resultSet.previous();
                                    }
                                }else
                                    outputStream.write(images);
                            }
                            if (!ios)
                                outputStream.write(100);
                            response.setStatus(SUCCESS);
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                            response.setStatus(FAILED);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(FAILED);
                }
                break;
            case GET_APK:
                FileHandler.sendAPK(response);
                break;
        }
    }

    private static void makeStatement(String sql,String[] setter,Resultable handler ){
        try (Connection connection = getConn()){
            try (PreparedStatement statement = connection.prepareStatement(sql)){
                if (setter != null && setter.length > 0){
                    for (int i = 0; i < setter.length; i++)
                        statement.setString(i + 1,setter[i]);
                    try (ResultSet resultSet = statement.executeQuery()){
                        handler.handleResultSet(connection,resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConn() throws SQLException {
        String db = "mydb";
        String connection = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false";
        String user = "root";
        String password = "we27308";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(connection, user, password);
    }
}
enum Actions{
    LOGIN,REGISTER,GET_PIC,SET_PIC,GET_USERS,GET_APK
}
