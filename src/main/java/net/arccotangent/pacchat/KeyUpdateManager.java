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

package net.arccotangent.pacchat;

import net.arccotangent.pacchat.logging.Logger;

import java.util.Collection;
import java.util.HashMap;

public class KeyUpdateManager {
	
	private static final HashMap<Long, KeyUpdate> incoming_updates = new HashMap<>();
	private static final HashMap<Long, KeyUpdate> outgoing_updates = new HashMap<>();
	private static final Logger kum_log = new Logger("KEY UPDATES");
	
	public static void addPendingUpdate(long id, KeyUpdate update) {
		if (!incoming_updates.containsKey(id)) {
			incoming_updates.put(id, update);
			kum_log.i("Pending update added to list: ID " + id + ", IP " + update.getAddress());
			kum_log.w("**********************************************");
			kum_log.w("NOTICE: Updating this key will permanently delete the old key.");
			kum_log.w("To accept this update, run this command: ua " + id);
			kum_log.w("To reject this update, run this command: ur " + id);
			kum_log.w("**********************************************");
		} else {
			kum_log.e("Attempted to add an update with ID " + id + " and source " + update.getAddress() + " to incoming update database, but it already exists! Please report this error to developers.");
		}
	}
	
	public static void addOutgoingUpdate(long id, KeyUpdate update) {
		if (!outgoing_updates.containsKey(id)) {
			outgoing_updates.put(id, update);
			kum_log.d("Added key update with ID " + id + " to outgoing update database.");
		} else {
			kum_log.e("Attempted to add an update with ID " + id + " and source " + update.getAddress() + " to outgoing update database, but it already exists! Please report this error to developers.");
		}
	}
	
	public static void completeIncomingUpdate(long id, KeyUpdate newUpdate) {
		if (incoming_updates.containsKey(id) && !incoming_updates.get(id).isProcessed()) {
			incoming_updates.put(id, newUpdate);
		} else {
			kum_log.e("Attempted to complete incoming update " + id + " but it doesn't exist or is already processed! Please report this error to developers.");
		}
	}
	
	public static void completeOutgoingUpdate(long id, KeyUpdate newUpdate) {
		if (outgoing_updates.containsKey(id) && !outgoing_updates.get(id).isProcessed()) {
			outgoing_updates.put(id, newUpdate);
		} else {
			kum_log.e("Attempted to complete outgoing update " + id + " but it doesn't exist or is already processed! Please report this error to developers.");
		}
	}
	
	static Collection<Long> getAllIncomingKeys() {
		return incoming_updates.keySet();
	}
	
	static Collection<Long> getAllOutgoingKeys() {
		return outgoing_updates.keySet();
	}
	
	public static KeyUpdate getIncomingUpdate(long id) {
		if (incoming_updates.containsKey(id))
			return incoming_updates.get(id);
		else
			return null;
	}
	
	static KeyUpdate getOutgoingUpdate(long id) {
		if (outgoing_updates.containsKey(id))
			return outgoing_updates.get(id);
		else
			return null;
	}
	
}

