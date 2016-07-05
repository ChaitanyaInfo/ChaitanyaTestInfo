package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.RetrievalDataManager;

public class RetrievalDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String retrievalDataRequest = "";
	private String retrievalDataResponse = "";
	private RetrievalDataManager rdm = new RetrievalDataManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		retrievalDataRequest = request.getParameter("request");
		System.out.println("The request is : " + retrievalDataRequest);
		retrievalDataResponse = rdm.getRetrievalDataResponse(retrievalDataRequest,request);
		System.out.println("The response is : " + retrievalDataResponse);
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(retrievalDataResponse);

	}
}
