package com.test.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.user.utils.PropertiesUtil;

import net.sf.json.JSONObject;

public class UnitTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

		System.out.println("inside DOGET");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String finalURL ="";
		StringBuffer unitResponse = new StringBuffer();
		String serviceType = request.getParameter("ServiceType");
		String functionType = request.getParameter("FunctionType");
		String memberId=request.getParameter("MemberId");
		String memberCode=request.getParameter("MemberCode");
		String mobCode=request.getParameter("MobileCode");
		String mobNumber=request.getParameter("MobileNumber");
		String grpzCode = request.getParameter("GroupzCode");
		String selectionList = request.getParameter("SelectionList");
		String datapart = request.getParameter("Data");		
		StringBuffer testBuffer = new StringBuffer();
		String hostname = request.getServerName()+":"+request.getServerPort();
		System.out.println("Host name:"+hostname);	
		if((serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")))&& (functionType.equalsIgnoreCase(PropertiesUtil
									.getProperty("submitselectionforivrrecord")))){
		testBuffer.append("<xml>");
		testBuffer.append("<request>");
		testBuffer.append("<servicetype>"+serviceType+"</servicetype>");
		testBuffer.append("<functiontype>"+functionType+"</functiontype>");
		testBuffer.append("<membercode>"+memberCode+"</membercode>");
		testBuffer.append("<memberid>"+memberId+"</memberid>");
		testBuffer.append("<groupzlist><groupzcode>"+grpzCode+"</groupzcode></groupzlist>");
		testBuffer.append("<mobile><countrycode>"+mobCode+"</countrycode><mobilenumber>"+mobNumber+"</mobilenumber></mobile>");
		testBuffer.append("<data>"+datapart+"</data>");
		testBuffer.append(selectionList);
		testBuffer.append("</request>");
		testBuffer.append("</xml>");
		String requestStr =testBuffer.toString();
		System.out.println("Request XML for publishing:"+requestStr.toString());		
		finalURL = "http://"+hostname+"/RestAPI/PostingDataServlet?request="+URLEncoder.encode(requestStr);
		}
		else if((serviceType.equalsIgnoreCase(PropertiesUtil
				.getProperty("ivrselection")))&& (functionType.equalsIgnoreCase(PropertiesUtil
						.getProperty("retrievallistforivrrecord")))){
			testBuffer.append("<xml>");
			testBuffer.append("<request>");
			testBuffer.append("<servicetype>"+serviceType+"</servicetype>");
			testBuffer.append("<functiontype>"+functionType+"</functiontype>");
			testBuffer.append("<membercode>"+memberCode+"</membercode>");
			testBuffer.append("<memberid>"+memberId+"</memberid>");
			testBuffer.append("<mobile><countrycode>"+mobCode+"</countrycode><mobilenumber>"+mobNumber+"</mobilenumber></mobile>");
			testBuffer.append("<groupzcode>"+grpzCode+"</groupzcode>");
			testBuffer.append("<data>"+datapart+"</data>");
			testBuffer.append("</request>");
			testBuffer.append("</xml>");
			String requestStr =testBuffer.toString();
			System.out.println("Request XML for Retrieval:"+requestStr.toString());
			finalURL = "http://"+hostname+"/RestAPI/RetrievalDataServlet?request="+URLEncoder.encode(requestStr);
		}
		else {
			System.out.println("Invalid Service type and Function type");
			unitResponse.append("<xml><response><statuscode>8888</statuscode><statusmessage>Invalid Service type and Function type</statusmessage></response></xml>");
			return;

		}
		System.out.println("Final URL :"+finalURL);
		try {
		URL url = new URL(finalURL);
		URLConnection conn = (URLConnection) url.openConnection();
		((HttpURLConnection) conn).setRequestMethod("POST");
		InputStream is = conn.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));		
		String res="";			
			while ((res = in.readLine()) != null) {
				System.out.println(" 3. Resource processing :- " + res);
				unitResponse.append(res);				
		}

		in.close();

	} catch (MalformedURLException e) {
		e.printStackTrace();
		System.out.println("Inside MalformedUrl exception:- "
				+ e.getMessage());
		unitResponse.append("<xml><response><statuscode>9999</statuscode><statusmessage>Time out</statusmessage></response></xml>");

	} catch (ProtocolException e) {
		e.printStackTrace();
		System.out.println("Inside ProtocolException exception:- "
				+ e.getMessage());
		unitResponse.append("<xml><response><statuscode>9999</statuscode><statusmessage>Time out</statusmessage></response></xml>");

	} catch (IOException e) {
		e.printStackTrace();
		System.out.println("Inside IOException exception:- "
				+ e.getMessage());
		unitResponse.append("<xml><response><statuscode>9999</statuscode><statusmessage>Time out</statusmessage></response></xml>");

	} catch (Exception e) {
		e.printStackTrace();
		System.out.println("Inside Exception" + e.getMessage());		
		unitResponse.append("<xml><response><statuscode>9999</statuscode><statusmessage>Time out</statusmessage></response></xml>");

	}
	System.out.println(" 5. Final Info :- " + unitResponse.toString());
		PrintWriter writer = response.getWriter();
		writer.append(unitResponse.toString());
	}

}
