package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.UserRegistrationManager;

public class UserRegistrationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String groupzsyncRequest = "";
	private String groupzsyncResponse = "";
	private UserRegistrationManager urm = new UserRegistrationManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		groupzsyncRequest = request.getParameter("request");
		System.out.println("The request is : " + groupzsyncRequest);
		groupzsyncResponse = urm.addNewUser(groupzsyncRequest,request);
		System.out.println("The response is : " + groupzsyncResponse);
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(groupzsyncResponse);

	}

}
