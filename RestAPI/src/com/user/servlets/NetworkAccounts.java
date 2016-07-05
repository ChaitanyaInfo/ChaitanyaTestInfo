package com.user.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.manager.NetworkAccountManager;

public class NetworkAccounts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String networkAccountRequest = "";
	private String networkAccountResponse = "";
	private NetworkAccountManager nam = new NetworkAccountManager();

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		networkAccountRequest = request.getParameter("request");
		System.out.println("The request is : " + networkAccountRequest);
		networkAccountResponse = nam.getNetworkAccounts(networkAccountRequest,request);
		System.out.println("The response is : " + networkAccountResponse);
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();	
		writer.append(networkAccountResponse);

		
	}
}
