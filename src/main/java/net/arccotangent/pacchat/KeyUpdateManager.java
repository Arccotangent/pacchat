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
	
	//TODO add HashMap for outgoing updates
	private static final HashMap<Long, KeyUpdate> incoming_updates = new HashMap<>();
	private static final Logger kum_log = new Logger("KEY UPDATES");
	
	public static void addPendingUpdate(long id, KeyUpdate update) {
		if (!incoming_updates.containsKey(id)) {
			incoming_updates.put(id, update);
		}
		kum_log.i("Pending update added to list: ID " + id + ", IP " + update.getSource());
		kum_log.w("**********************************************");
		kum_log.w("NOTICE: Updating this key will permanently delete the old key.");
		kum_log.w("To accept this update, run this command: ua " + id);
		kum_log.w("To reject this update, run this command: ur " + id);
		kum_log.w("**********************************************");
	}
	
	public static void completeIncomingUpdate(long id, KeyUpdate newUpdate) {
		if (incoming_updates.containsKey(id) && incoming_updates.get(id).isProcessed()) {
			incoming_updates.put(id, newUpdate);
		}
	}
	
	static Collection<Long> getAllIncomingKeys() {
		return incoming_updates.keySet();
	}
	
	public static KeyUpdate getUpdate(long id) {
		if (incoming_updates.containsKey(id))
			return incoming_updates.get(id);
		else
			return null;
	}
	
}

