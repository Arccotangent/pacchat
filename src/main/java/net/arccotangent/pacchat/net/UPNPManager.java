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

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.logging.Logger;
import net.arccotangent.pacchat.net.p2p.P2PServer;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

public class UPNPManager {
	
	private static final Logger upnp_log = new Logger("UPNP");
	private static final Logger registry_log = new Logger("UPNP/REGISTRY");
	private static final Logger control_point_log = new Logger("UPNP/CONTROL-POINT");
	private static UpnpService UPNP_SERVICE = null;
	private static boolean upnp_open = false;
	
	public static boolean isOpen() {
		return upnp_open;
	}
	
	public static void UPNPOpenPorts()
	{
		PortMapping[] ports = new PortMapping[2];
		upnp_log.i("Listing ports to be mapped");
		ports[0] = new PortMapping(Server.PORT, NetUtils.getLocalIPAddr(), PortMapping.Protocol.TCP, "PacChat " + Main.VERSION + " TCP");
		ports[1] = new PortMapping(P2PServer.P2P_PORT, NetUtils.getLocalIPAddr(), PortMapping.Protocol.TCP, "PacChat " + Main.VERSION + " P2P TCP");
		//arr[index] = new PortMapping(port, ipaddr, protocol, description);
		for (int i = 0; i < ports.length; i++)
		{
			upnp_log.i("Mapping " + i + ": Port " + ports[i].getExternalPort() + ", Protocol " + ports[i].getProtocol().toString());
		}
		upnp_log.i("Initializing UPNP service.");
		PortMappingListener pml = new PortMappingListener(ports);
		upnp_log.i("Registering port mappings.");
		UPNP_SERVICE = new UpnpServiceImpl(pml);
		//UPNP_SERVICE.getRegistry().addListener(REGISTRY_LISTENER);
		registry_log.i("Advertising local services.");
		UPNP_SERVICE.getRegistry().advertiseLocalDevices();
		control_point_log.i("Sending search message to all devices and services, devices should respond soon.");
		control_point_log.i("This function's purpose is to try and find the router, then forward ports through it.");
		UPNP_SERVICE.getControlPoint().search(new STAllHeader());
		upnp_log.i("If UPNP is to work, it will in a matter of seconds. Otherwise, UPNP is likely disabled on your network.");
		//upnp_log.w("If it doesn't work, it should within a matter of seconds. You should've gotten some discovery messages. If no discovery messages are printed, something is wrong.");
		upnp_open = true;
	}
	
	public static void UPNPClosePorts()
	{
		upnp_log.i("Shutting down port forwarding service.");
		UPNP_SERVICE.shutdown();
		upnp_log.i("Port forwarding service shut down.");
		upnp_open = false;
	}
	
}
