package servlet;

import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutServlet extends ChatServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = (String) request.getSession().getAttribute("name");

        if (name != null) {
            ChatUser aUser = activeUsers.get(name);

            if (aUser.getSessionId().equals((String) request.getSession().getId())) {
                synchronized (activeUsers) {
                    activeUsers.remove(name);
                }
                request.getSession().setAttribute("name", null);
                response.addCookie(new Cookie("sessionId", null));
                getServletContext().setAttribute("first",false);
                getServletContext().setAttribute("i",0);
                response.sendRedirect(response.encodeRedirectURL("/lab_8/chat/login"));
            } else {
                response.sendRedirect(response.encodeRedirectURL("/lab_8/chat/messages"));
            }
        } else {
            response.sendRedirect(response.encodeRedirectURL("/lab_8/chat/messages"));
        }
    }
}
