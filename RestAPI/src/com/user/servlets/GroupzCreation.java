package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.GroupzCreationManager;
import com.user.manager.NetworkAccountManager;

public class GroupzCreation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String groupzCreationRequest = "";
	private String groupzCreatioResponse = "";
	private GroupzCreationManager gcm = new GroupzCreationManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		groupzCreationRequest = request.getParameter("request");
		System.out.println("The request is : " + groupzCreationRequest);
		groupzCreatioResponse = gcm.createGroupz(groupzCreationRequest,request);
		System.out.println("The response is : " + groupzCreatioResponse);
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();	
		writer.append(groupzCreatioResponse);
	}
		

}
