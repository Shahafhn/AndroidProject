package main;


import java.io.IOException;


public class MainServlet extends javax.servlet.http.HttpServlet {

    public static final String ADD = "add";
    public static final String UPLOAD_PHOTO = "upload";
    public final int SUCCESS = 200;
    public final int FAILED = 300;
    public final int USER_EXISTS = 401;
    public final int USER_NOT_FOUND = 404;
    public final String LOGIN = "login";
    public final String CREATE_NEW_USER = "register";
    public final String SEARCH_FOR_ACCOUNTS = "search";
    public final String GET_PICTURE = "getpic";

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String action = request.getParameter("action");
        switch (action){
            case UPLOAD_PHOTO:
                if (ActionsManager.saveImage(request,response))
                    response.setStatus(SUCCESS);
                else
                    response.setStatus(FAILED);
                return;
            case GET_PICTURE:
                if (ActionsManager.sendImage(request,response))
                    response.setStatus(SUCCESS);
                else
                    response.setStatus(FAILED);
                return;
        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String action = request.getParameter("action");
        switch (action) {
            case SEARCH_FOR_ACCOUNTS:
                if (ActionsManager.sendUserList(request,response))
                    response.setStatus(SUCCESS);
                else
                    response.setStatus(FAILED);
                return;
            case ADD:
                String name = request.getParameter("name");
                if (ActionsManager.insertName(name))
                        response.setStatus(SUCCESS);
                else
                        response.setStatus(USER_EXISTS);
                return;
            case LOGIN:
                if (ActionsManager.login(false,false,request))
                    response.setStatus(SUCCESS);
                else
                    response.setStatus(USER_NOT_FOUND);
                return;
            case CREATE_NEW_USER:
                if (ActionsManager.login(true,false,request))
                    response.setStatus(SUCCESS);
                else
                    response.setStatus(USER_EXISTS);
                return;
            case "download":
                ActionsManager.sendAPK(response,"ProjectGram");
                return;
            case "latest":
                ActionsManager.sendAPK(response,"latest");
                break;
            case "debug":
                ActionsManager.sendAPK(response,"debug");
                break;
        }
    }
}
