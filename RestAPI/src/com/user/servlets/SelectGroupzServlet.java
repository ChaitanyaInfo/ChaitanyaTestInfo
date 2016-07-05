package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.SelectGroupzManager;
import com.user.manager.UserRegistrationManager;

public class SelectGroupzServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String groupzRequestList = "";
	private String groupzResponseList = "";
	private SelectGroupzManager sgm = new SelectGroupzManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		groupzRequestList = request.getParameter("request");
		System.out.println("The request is : " + groupzRequestList);
		groupzResponseList = sgm.getGroupzAccount(groupzRequestList,request);
		System.out.println("The response is : " + groupzResponseList);
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(groupzResponseList);

	}

}
