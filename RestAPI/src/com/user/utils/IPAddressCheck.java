package com.user.utils;

import java.util.ArrayList;
import java.util.List;

public class IPAddressCheck {

	public boolean checkIPAddressMatch(String ipOne, String ipTwo) {
		if (ipTwo == null || ipOne == null)
			return false;
		if (ipTwo.isEmpty() == true || ipOne.isEmpty() == true)
			return false;

		String ipOneSplit[] = ipOne.split("\\.");
		String ipTwoSplit[] = ipTwo.split("\\.");

		System.out.println("Testing " + ipOne + " and " + ipTwo);
		// System.out.println("Testing Length " + ipOneSplit.length + " and " +
		// ipTwoSplit.length ) ;
		if (ipOneSplit != null && ipOneSplit.length != 4)
			return false;
		if (ipTwoSplit != null && ipTwoSplit.length != 4)
			return false;

		for (int i = 0; i < 4; i++) {
			// System.out.println("Testing " + ipOneSplit[i] + " and " +
			// ipTwoSplit[i] ) ;
			if (ipOneSplit[i].equals("*") || ipTwoSplit[i].equals("*"))
				continue;
			if (ipOneSplit[i].equals(ipTwoSplit[i]) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean checkIPAddressInList(String ipAddress,
			List<String> ipAddressList) {

		for (String ipAddr : ipAddressList) {
			if (checkIPAddressMatch(ipAddress, ipAddr) == true)
				return true;
		}
		return false;
	}

	public static void main(String[] args) {
		IPAddressCheck ip = new IPAddressCheck();

		List<String> ipAddresses = new ArrayList<String>();

		ipAddresses.add("198.166.5.8");
		ipAddresses.add("192.168.1.5");
		ipAddresses.add("172.167.1.*");
		ipAddresses.add("173.167.*.*");
		ipAddresses.add("174.*.*.*");
		// ipAddresses.add("*.*.*.*" ) ;

		boolean retValue;
		retValue = ip.checkIPAddressInList("198.166.1.5", ipAddresses);
		System.out.println("The result is : " + retValue);
		retValue = ip.checkIPAddressInList("192.168.1.5", ipAddresses);
		System.out.println("The result is : " + retValue);
		retValue = ip.checkIPAddressInList("198.166.5.8", ipAddresses);
		System.out.println("The result is : " + retValue);
		retValue = ip.checkIPAddressInList("172.167.1.5", ipAddresses);
		System.out.println("The result is : " + retValue);
		retValue = ip.checkIPAddressInList("198.166.1.5", ipAddresses);
		System.out.println("The result is : " + retValue);
	}

}
