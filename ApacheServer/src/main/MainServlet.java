package main;

import java.io.*;


public class MainServlet extends javax.servlet.http.HttpServlet {

    private static final String UPLOAD_PHOTO = "upload";
    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String SEARCH_FOR_ACCOUNTS = "search";
    private static final String GET_PICTURE = "getpic";

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String action = request.getParameter("action");
        switch (action) {
            case UPLOAD_PHOTO:
                ActionsManager.gotAction(Actions.SET_PIC,request,response);
                break;
            case GET_PICTURE:
                ActionsManager.gotAction(Actions.GET_PIC,request,response);
                break;
        }
    }


    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String action = request.getParameter("action");
        switch (action) {
            case SEARCH_FOR_ACCOUNTS:
                ActionsManager.gotAction(Actions.GET_USERS,request,response);
                break;
            case LOGIN:
                ActionsManager.gotAction(Actions.LOGIN,request,response);
                break;
            case REGISTER:
                ActionsManager.gotAction(Actions.REGISTER,request,response);
                break;
        }
    }
}
