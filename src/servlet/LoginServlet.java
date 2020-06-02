package servlet;

import entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

public class LoginServlet extends ChatServlet {
    private static final long serialVersionUID = 1L;
    private int sessionTimeout = 10 * 60;

    public void init() throws ServletException {
        super.init();

        String value = getServletConfig().getInitParameter("SESSION_TIMEOUT");

        if (value != null) {
            sessionTimeout = Integer.parseInt(value);
        }

        getServletContext().setAttribute("i", 0);
        getServletContext().setAttribute("first", false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    boolean first = (boolean) getServletContext().getAttribute("first");
                    while (first) {
                        int i = (int) getServletContext().getAttribute("i");
                        getServletContext().setAttribute("i", ++i);
                        first = (boolean) getServletContext().getAttribute("first");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = (String) request.getSession().getAttribute("name");

        String errorMessage = (String) request.getSession().getAttribute("error");

        String previousSessionId = null;

        if (name != null && !"".equals(name)) {
            getServletContext().setAttribute("i", 0);
            errorMessage = processLogonAttempt(name, request, response);
        }

        response.setCharacterEncoding("utf8");

        PrintWriter pw = response.getWriter();

        pw.println("<html><head><meta http-equiv='Content-Type'\"" + 
        		"\" content='text/html; charset=utf-8'/>"
        		+ "<link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3.css\"><title>chat: log-in</title></head><body class=\"w3-light-grey\">");

        if (errorMessage != null) {
            pw.println("<span onclick=\"this.parentElement.style.display='none'\"></span>\n" +
                    "   <h5>" + errorMessage + "</h5>");
        }

        pw.println("<div class=\"w3-card-4\">\n" +
                "        <div class=\"w3-container w3-center w3-green\">\n" +
                "        </div>\n" +
                "        <form method=\"post\" class=\"w3-selection w3-light-grey\">\n" +
                "            <label>Введите ваше имя:\n" +
                "                <input type=\"text\" name=\"name\" class=\"w3-input w3-animate-input w3-border w3-round-large\" style=\"width: 30%\"><br />\n" +
                "            </label>\n" +
                "            <button type=\"submit\" >Войти</button>\n" +
                "        </form>\n" +
                "    </div>");

        pw.println("</form></body></html>");

        request.getSession().setAttribute("error", null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = (String) request.getParameter("name");

        String errorMessage = null;

        if (name == null || "".equals(name)) {
            errorMessage = "Имя пользователя не может быть пустым";
        } else {
            errorMessage = processLogonAttempt(name, request, response);
        }

        if (errorMessage != null) {
            request.getSession().setAttribute("name", null);
            request.getSession().setAttribute("error", errorMessage);
            response.sendRedirect(response.encodeRedirectURL("/lab_8/chat/login"));
        }
    }

    String processLogonAttempt(String name, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sessionId = request.getSession().getId();

        ChatUser aUser = activeUsers.get(name);

        if (aUser == null) {
            aUser = new ChatUser(name, Calendar.getInstance().getTimeInMillis(), sessionId);
            synchronized (activeUsers) {
                activeUsers.put(aUser.getName(), aUser);
            }
        }

        if (aUser.getSessionId().equals(sessionId) ||
                aUser.getLastInteractionTime() < (Calendar.getInstance().getTimeInMillis() - sessionTimeout * 1000)) {

            request.getSession().setAttribute("name", name);

            aUser.setLastInteractionTime(Calendar.getInstance().getTimeInMillis());

            Cookie sessionIdCookie = new Cookie("sessionId", sessionId);

            sessionIdCookie.setMaxAge(60 * 60 * 24 * 365);

            response.addCookie(sessionIdCookie);

            response.sendRedirect(response.encodeRedirectURL("/lab_8/chat/messages"));
            return null;

        } else {
            return "Извините, но имя <strong>" + name + "</strong> уже занято";
        }
    }

}
