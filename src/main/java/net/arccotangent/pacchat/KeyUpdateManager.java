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
	
	//TODO add HashMap for both outgoing and incoming, and both pending and completed updates
	private static HashMap<Long, KeyUpdate> pending_updates = new HashMap<>();
	private static Logger kum_log = new Logger("KEY UPDATES");
	
	public static void addPendingUpdate(long id, KeyUpdate update) {
		if (!pending_updates.containsKey(id)) {
			pending_updates.put(id, update);
		}
		kum_log.i("Pending update added to list: ID " + id + ", IP " + update.getSource());
		kum_log.w("**********************************************");
		kum_log.w("NOTICE: Updating this key will permanently delete the old key.");
		kum_log.w("**********************************************");
	}
	
	static void updatePendingUpdate(long id, KeyUpdate newUpdate) {
		if (pending_updates.containsKey(id) && !pending_updates.get(id).isProcessed()) {
			pending_updates.put(id, newUpdate);
		}
	}
	
	static Collection<Long> getAllKeys() {
		return pending_updates.keySet();
	}
	
	public static void removeCompletedUpdate(long id) {
		if (pending_updates.get(id).isProcessed()) {
			pending_updates.remove(id);
		}
	}
	
	public static KeyUpdate getPendingUpdate(long id) {
		if (pending_updates.containsKey(id)) {
			return pending_updates.get(id);
		} else {
			return null;
		}
	}
	
}

