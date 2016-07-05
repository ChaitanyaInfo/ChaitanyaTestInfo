package com.ServiceRequest.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ServiceRequest.manager.ServiceRequestManager;

public class ServiceRequestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String srRequestList = "";
	private String srResponseList = "";
	private ServiceRequestManager srm  = new ServiceRequestManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		srRequestList = request.getParameter("request");
		System.out.println("\n The SR request is : " + srRequestList);
		
		srResponseList = srm.processSR(srRequestList,request);
		
		System.out.println("\n The SR response is : " + srResponseList);
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(srResponseList);

	}

}
