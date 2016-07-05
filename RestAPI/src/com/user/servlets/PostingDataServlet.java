package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.PostingDataManager;

public class PostingDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String postingDataRequest = "";
	private String postingDataResponse = "";
	private PostingDataManager pdpm = new PostingDataManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		postingDataRequest = request.getParameter("request");
		System.out.println("The request is : " + postingDataRequest);
		postingDataResponse = pdpm.getPostingDataResponse(postingDataRequest,request);
		System.out.println("The response is : " + postingDataResponse);
		//response.setContentType("text/xml;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		//writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");		
		writer.append(postingDataResponse);

	}
}
