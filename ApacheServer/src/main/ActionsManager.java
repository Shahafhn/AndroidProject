package main;


import java.io.*;
import java.nio.ByteBuffer;
import java.sql.*;

public abstract class ActionsManager {

    private final static String imageDirectory = "C:\\Users\\Public\\Documents\\ProjectGram\\";

    public static boolean login(boolean isNewUser, boolean auth, javax.servlet.http.HttpServletRequest request) {
        String username = request.getParameter("user");
        String password = request.getParameter("pass");
        if (isNewUser)
            return insertNewUser(username, password);
        return selectLogin(auth, username, password);
    }

    public static boolean sendUserList(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
        String search = request.getParameter("look");
        OutputStream outputStream = null;
        if (login(false, true, request)) {
            try (Connection conn = getConn(true)) {
                try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users_table WHERE username LIKE ?")) {
                    statement.setString(1, "%" + search + "%");
                    try (ResultSet resultSet = statement.executeQuery()) {
                        outputStream = response.getOutputStream();
                        while (resultSet.next()) {
                            String username = resultSet.getString(1);
                            int numOfImages = resultSet.getInt(3);
                            outputStream.write(username.length());
                            outputStream.write(username.getBytes());
                            outputStream.write(numOfImages);
                        }
                        outputStream.write(100);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }


    public static boolean sendImage(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
        if (login(false, true, request)) {
            int position = Integer.valueOf(request.getParameter("position"));
            String profile = request.getParameter("profile");
            File file = new File(imageDirectory + profile + "\\" + profile + "(" + position + ").jpg");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            if (file.isFile()) {
                try {
                    inputStream = new FileInputStream(file);
                    response.setContentType("application/octet-stream");
                    outputStream = response.getOutputStream();
                    byte[] bytes = new byte[4];
                    int actuallyRead;
                    ByteBuffer.wrap(bytes).putInt((int) file.length());
                    outputStream.write(bytes);
                    bytes = new byte[2048];
                    while ((actuallyRead = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, actuallyRead);
                    }
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            System.out.println("___________ " + request.getParameter("user"));
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void sendAPK(javax.servlet.http.HttpServletResponse response, String apk) {
        String path = "D:\\HackerU\\GitHub\\Android\\ProjectGram\\app\\build\\outputs\\apk\\debug\\";
        OutputStream outputStream = null;
        InputStream inputStream = null;
        File file = new File(path + apk + ".apk");
        response.setContentType("application/apk");
        response.setHeader("Content-disposition", "attachment; filename=ProjectGram.apk");
        try {
            outputStream = response.getOutputStream();
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[2048];
            int actuallyRead;
            while ((actuallyRead = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, actuallyRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean saveImage(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
        String user = request.getParameter("user");
        if (login(false, true, request)) {
            System.out.println("receiving picture: " + user);
            String userPath = imageDirectory + user;
            File dir = new File(userPath);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            for (int i = 0; i < 25; i++) {
                File file = new File(userPath + "\\" + user + "(" + i + ")" + ".jpg");
                if (!file.isFile()) {
                    InputStream inputStream = null;
                    OutputStream fileOutputStream = null;
                    OutputStream outputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);
                        inputStream = request.getInputStream();
                        outputStream = response.getOutputStream();
                        int read = 0;
                        byte[] bytes = new byte[4096];
                        byte[] fileSize = new byte[4];
                        int actuallyRead;
                        while ((actuallyRead = inputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, actuallyRead);
                            read += actuallyRead;
                            ByteBuffer.wrap(fileSize).putInt(read);
                            outputStream.write(fileSize);
                        }
                        if (file.isFile() && file.length() > 0) {
                            updateImageCount(user);
                            return true;
                        } else {
                            fileOutputStream.close();
                            file.delete();
                            System.out.println("failed to download empty file: " + user);
                            break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        file.delete();
                        e.printStackTrace();
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void updateImageCount(String name) {
        try (Connection conn = getConn(true)) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users_table WHERE username=?")) {
                statement.setString(1, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int numOfImages = resultSet.getInt(3);
                        try (PreparedStatement preparedStatement = conn.prepareStatement("UPDATE users_table SET images=? WHERE username=?")) {
                            preparedStatement.setInt(1, numOfImages + 1);
                            preparedStatement.setString(2, name);
                            if (preparedStatement.executeUpdate() == 1)
                                System.out.println("user " + name + " images: " + (numOfImages + 1));
                            else
                                System.out.println("failed to update");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean insertName(String name) {
        try (Connection conn = getConn(false)) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM all_names WHERE name=?")) {
                statement.setString(1, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        try (PreparedStatement statement1 = conn.prepareStatement("INSERT INTO all_names (name) VALUES (?)")) {
                            statement1.setString(1, name);
                            int rowsAffected = statement1.executeUpdate();
                            if (rowsAffected != 0) {
                                System.out.println("new name: " + name);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("add name attempt: " + name);
        return false;
    }

    private static boolean insertNewUser(String username, String password) {
        try (Connection conn = getConn(true)) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users_table WHERE username=?")) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        try (PreparedStatement statement1 = conn.prepareStatement("INSERT INTO users_table (username,password,images) VALUES (?,?,?)")) {
                            statement1.setString(1, username);
                            statement1.setString(2, password);
                            statement1.setString(3, String.valueOf(0));
                            int rowsAffected = statement1.executeUpdate();
                            if (rowsAffected != 0) {
                                System.out.println("register: " + username + " " + password);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("register failed: " + username);
        return false;
    }

    private static boolean selectLogin(boolean authentication, String username, String password) {
        try (Connection connection = getConn(true)) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_table WHERE username=? AND password=?")) {
                statement.setString(1, username);
                statement.setString(2, password);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        if (!authentication)
                            System.out.println("login: " + username);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!authentication)
            System.out.println("login failed: " + username + " " + password);
        return false;
    }

    private static Connection getConn(boolean isMyDB) throws SQLException {
        String db = isMyDB ? "project_db" : "shir_db";
        String connection = "jdbc:mysql://localhost:3306/" + db + "?useSSL=false";
        String user = "root";
        String password = "wangm262";
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(connection, user, password);
    }
}
