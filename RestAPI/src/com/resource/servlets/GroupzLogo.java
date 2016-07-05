package com.resource.servlets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.resource.manager.ResourceFileManager;
import com.user.utils.RestUtils;

public class GroupzLogo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private static final String URLPATTERN = "/GroupzLogo/";
	ResourceFileManager rfm = new ResourceFileManager();
	RestUtils restUtils = new RestUtils();

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuffer urlString = request.getRequestURL();		
	//	String FinalURL = restUtils.getValidURL(urlString.toString());
		System.out.println("Final URL:"+urlString);	
		int index = urlString.indexOf(URLPATTERN);
		if (index > 0) {
			index = index + URLPATTERN.length();
		} else {
			// Error path should be coded
			System.out.println("Exit");
			return;
		}	
		System.out.println("URL STRING:"+urlString);
		String code = urlString.substring(index);
		System.out.println("Code:" + code);
		boolean codeStatus = rfm.checkValidGroupzCode(code);
		System.out.println("Code Status:" + codeStatus);
		if (codeStatus == true) {
			byte[] logoImage = rfm.getGroupzLogo(code);
			if (logoImage != null) {
				response.getOutputStream().write(logoImage);
				response.getOutputStream().close();
			} else {
				try {
					StringBuffer totalURL = request.getRequestURL();
					totalURL.delete(totalURL.lastIndexOf("/GroupzLogo"),
							totalURL.length());
				//	System.out.println("Total URL:" + totalURL);

					String imageUrl = totalURL + "/images/home.png";
					//System.out.println("Image URL:" + imageUrl);
					URL url = new URL(imageUrl);
					BufferedImage img = ImageIO.read(url);
					OutputStream out = response.getOutputStream();
					ImageIO.write(img, "png", out);
					out.close();
				} catch (IOException e) {
				}
			}
		} else {
			System.out.println("Invalid Code");
			PrintWriter pw = response.getWriter();
			pw.write("Invalid code");
			return;
		}

	}
}
