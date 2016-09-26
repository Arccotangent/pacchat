package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.logging.Logger;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetUtils {
	
	private static Logger nu_log = new Logger("NETWORK");
	private static String ip_address = "";
	
	static String getLocalIPAddr() {
		return ip_address;
	}
	
	public static void updateLocalIPAddr()
	{
		nu_log.i("Retrieving local IP address.");
		ip_address = "";
		
		if (System.getProperty("os.name").contains("Linux"))
		{
			ArrayList<Inet4Address> i4 = new ArrayList<>();
			Enumeration<NetworkInterface> e = null;
			try {
				e = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			
			assert e != null;
			
			while(e.hasMoreElements())
			{
				NetworkInterface n = e.nextElement();
				System.out.print(n.getName() + ": ");
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements())
				{
					InetAddress i = ee.nextElement();
					if (i instanceof Inet4Address)
					{
						System.out.println(i.getHostAddress());
						i4.add((Inet4Address) i);
					}
				}
			}
			for (Inet4Address address : i4) {
				if (!address.isLoopbackAddress() && !address.getHostAddress().startsWith("127.")) {
					nu_log.i("[LINUX] Found seemingly valid IPv4 address in enumeration: " + address.getHostAddress());
					ip_address = address.getHostAddress();
					return;
				}
			}
			if (ip_address.isEmpty())
			{
				try {
					ip_address = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
			nu_log.i("[LINUX] IP address detected as: " + ip_address);
		}
		else
		{
			try {
				ip_address = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			nu_log.i("[NON-LINUX] IP address detected as: " + ip_address);
		}
	}
	
}
