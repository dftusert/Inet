package com.user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;

public class Main extends HttpServlet {
    public String getGreeting() {
        return "Hello world.";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // https://www.oracle.com/technetwork/java/servlet-142430.html
        PrintWriter out = resp.getWriter();
        out.println(this.getGreeting());
        out.close();
    }
}
