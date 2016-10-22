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

public class KeyUpdate {
	
	private String source;
	private boolean accepted = false;
	private boolean processed = false;
	
	public KeyUpdate(String source_ip) {
		source = source_ip;
	}
	
	String getSource() {
		return source;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public boolean isAccepted() {
		while (!processed) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return accepted;
	}
	
	void acceptUpdate() {
		processed = true;
		accepted = true;
	}
	
	void rejectUpdate() {
		processed = true;
	}
	
}
