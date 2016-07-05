package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.AuthenticateManager;


public class Authentication extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String authenticationRequest = "";
	private String authenticationResponse = "";
	private AuthenticateManager am = new AuthenticateManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		authenticationRequest = request.getParameter("request");
		System.out.println("The request is : " + authenticationRequest);
		authenticationResponse = am.authenticate(request,authenticationRequest);
		System.out.println("The response is : " + authenticationResponse);
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();	
		writer.append(authenticationResponse);
	}
}
