/*
This file is part of PacChat.

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PacChat is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PacChat.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetUtils {
	
	private static final Logger nu_log = new Logger("NETWORK");
	private static String ip_address = "";
	private static String external_ip = "";
	
	static String getLocalIPAddr() {
		return ip_address;
	}
	
	public static String getExternalIPAddr() {
		return external_ip;
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
						if (n.getName().toLowerCase().startsWith("tun")) {
							nu_log.w("Ignoring tunnel interface " + n.getName());
						} else {
							i4.add((Inet4Address) i);
						}
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
	
	public static void updateExternalIPAddr() {
		nu_log.i("Retrieving external IP address.");
		CloseableHttpClient cli = HttpClients.createDefault();
		
		HttpGet req = new HttpGet("http://checkip.amazonaws.com");
		
		try {
			CloseableHttpResponse res = cli.execute(req);
			
			BasicResponseHandler handler = new BasicResponseHandler();
			external_ip = handler.handleResponse(res);
		} catch (IOException e) {
			nu_log.e("Error while retrieving external IP!");
			e.printStackTrace();
		}
	}
	
}
