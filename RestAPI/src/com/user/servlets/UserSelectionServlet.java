package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.SelectGroupzManager;
import com.user.manager.UserSelectionManager;

public class UserSelectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String usrSelectionRequest = "";
	private String usrSelectionResponse = "";
	private UserSelectionManager usm = new UserSelectionManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		usrSelectionRequest = request.getParameter("request");
		System.out.println("The request is : " + usrSelectionRequest);
		usrSelectionResponse = usm.getSelectionList(usrSelectionRequest,request);
		System.out.println("The response is : " + usrSelectionResponse);
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(usrSelectionResponse);

	}

}
