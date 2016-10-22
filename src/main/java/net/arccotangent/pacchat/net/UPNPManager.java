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
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

//import org.fourthline.cling.model.meta.LocalDevice;
//import org.fourthline.cling.model.meta.RemoteDevice;
//import org.fourthline.cling.registry.Registry;
//import org.fourthline.cling.registry.RegistryListener;

public class UPNPManager {
	
	private static Logger upnp_log = new Logger("UPNP");
	private static Logger registry_log = new Logger("UPNP/REGISTRY");
	private static Logger control_point_log = new Logger("UPNP/CONTROL-POINT");
	private static UpnpService UPNP_SERVICE = null;
	private static boolean upnp_open = false;
	
	public static boolean isOpen() {
		return upnp_open;
	}
	
	/*
	private static RegistryListener REGISTRY_LISTENER = new RegistryListener() {
		
		public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
			registry_log.i("Discovery started: " + device.getDisplayString());
		}
		
		public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
			registry_log.e("Discovery failed: " + device.getDisplayString() + " => " + ex);
		}
		
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			registry_log.i("Remote device available: " + device.getDisplayString());
		}
		
		public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
			registry_log.i("Remote device updated: " + device.getDisplayString());
		}
		
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			registry_log.w("Remote device removed: " + device.getDisplayString());
		}
		
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			registry_log.i("Local device added: " + device.getDisplayString());
		}
		
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			registry_log.w("Local device removed: " + device.getDisplayString());
		}
		
		public void beforeShutdown(Registry registry) {
			registry_log.i("Before shutdown, the registry has devices: " + registry.getDevices().size());
		}
		
		public void afterShutdown() {
			registry_log.i("Shutdown of registry complete!");
			
		}
	};
	*/
	
	static void UPNPOpenPorts()
	{
		PortMapping[] ports = new PortMapping[1];
		upnp_log.i("Listing ports to be mapped");
		ports[0] = new PortMapping(Server.PORT, NetUtils.getLocalIPAddr(), PortMapping.Protocol.TCP, "PacChat " + Main.VERSION + " TCP");
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
