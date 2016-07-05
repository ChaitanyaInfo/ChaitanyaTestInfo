package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.AuthenticateManager;
import com.user.manager.GCMUpdateManager;

public class GCMUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String gcmRequest = "";
	private String gcmResponse = "";
	private GCMUpdateManager gcm = new GCMUpdateManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		gcmRequest = request.getParameter("request");
		System.out.println("The request is : " + gcmRequest);
		gcmResponse = gcm.updateGCMId(request,gcmRequest);
		System.out.println("The response is : " + gcmResponse);
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();	
		writer.append(gcmResponse);
	}

}
