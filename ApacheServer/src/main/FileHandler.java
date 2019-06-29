package main;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;

abstract class FileHandler {

    private final static String directory = "/home/akraililo/projectgram/";
    private final static String imageDirectory = directory + "images/";

    static boolean sendImage(HttpServletRequest request, HttpServletResponse response, boolean ios) {
//        String imageDirectory = "C:\\Users\\Public\\Documents\\ProjectGram\\";
        int position = Integer.valueOf(request.getParameter("position"));
        String profile = request.getParameter("profile");
        File file = new File(imageDirectory + profile + "/" + profile + "(" + position + ").jpg");
        if (file.isFile()) {
            try (OutputStream outputStream = response.getOutputStream()){
                try (InputStream inputStream = new FileInputStream(file)) {
                    response.setContentType("application/octet-stream");
                    byte[] bytes = new byte[4];
                    int actuallyRead;
                    if (!ios) {
                        ByteBuffer.wrap(bytes).putInt((int) file.length());
                        outputStream.write(bytes);
                    }
                    bytes = new byte[2048];
                    while ((actuallyRead = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, actuallyRead);
                    }
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    static void sendAPK(HttpServletResponse response) {
//        String directory = "D:\\HackerU\\GitHub\\Android\\ProjectGram\\app\\build\\outputs\\apk\\debug\\";
        String apk = "projectgram";
        File file = new File(directory + apk + ".apk");
        response.setContentType("application/apk");
        response.setHeader("Content-disposition", "attachment; filename=ProjectGram.apk");
        try (OutputStream outputStream = response.getOutputStream()){
            try (InputStream inputStream = new FileInputStream(file)){
                byte[] bytes = new byte[2048];
                int actuallyRead;
                while ((actuallyRead = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, actuallyRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static File saveImage(String user, HttpServletRequest request) throws IOException{
//        String imageDirectory = "C:\\Users\\Public\\Documents\\ProjectGram\\";
        File dir = new File(imageDirectory + user);
        if (!dir.isDirectory())
            dir.mkdirs();

        for (int i = 0; i < 25; i++) {
            File file = new File(dir.toPath() + "\\" + user + "(" + i + ")" + ".jpg");
            if (!file.isFile() || file.length() == 0) {
                try (OutputStream fileOutputStream = new FileOutputStream(file)){
                    try (InputStream inputStream = request.getInputStream()) {
                        byte[] bytes = new byte[4096];
                        int actuallyRead;
                        while ((actuallyRead = inputStream.read(bytes)) != -1)
                            fileOutputStream.write(bytes, 0, actuallyRead);

                        if (file.isFile() && file.length() > 0)
                            return file;
                        else {
                            file.delete();
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

}
