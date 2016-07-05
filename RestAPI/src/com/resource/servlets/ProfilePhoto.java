package com.resource.servlets;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.resource.manager.ResourceFileManager;
import com.user.utils.RestUtils;

public class ProfilePhoto extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	ResourceFileManager rfm = new ResourceFileManager();
	RestUtils restUtils = new RestUtils();

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuffer urlString = request.getRequestURL();				
		int profileId = restUtils.extractId(urlString.toString());
		if (profileId == 0) {
			PrintWriter pe = response.getWriter();
			pe.write("Invalid URL");
		} else {
			boolean idStatus = rfm.checkValidProfile(profileId);
			if (idStatus == true) {
				byte[] profileImage = rfm.getProfilePhoto(profileId);
				if (profileImage != null) {
					response.getOutputStream().write(profileImage);
					response.getOutputStream().close();
				} else {
					try {
						StringBuffer totalURL = request.getRequestURL();
						totalURL.delete(totalURL.lastIndexOf("/ProfilePhoto"),
								totalURL.length());
						String imageUrl = totalURL + "/images/no_photo.png";						
						URL url = new URL(imageUrl);
						BufferedImage img = ImageIO.read(url);
						OutputStream out = response.getOutputStream();
						ImageIO.write(img, "png", out);
						out.close();
					} catch (IOException e) {
					}
				}
			} else {
				PrintWriter pe = response.getWriter();
				pe.write("Invalid ProfileId");
			}

		}
	}
}
