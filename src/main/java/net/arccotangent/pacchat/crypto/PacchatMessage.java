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

package net.arccotangent.pacchat.crypto;

public class PacchatMessage {
	
	private final String msg;
	private final boolean verify;
	private final boolean decrypt;
	
	PacchatMessage(String message, boolean verified, boolean decrypted) {
		msg = message;
		verify = verified;
		decrypt = decrypted;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public boolean isVerified() {
		return verify;
	}
	
	public boolean isDecryptedSuccessfully() {
		return decrypt;
	}
	
}

class DecryptStatus {
	
	private final byte[] message;
	private final boolean decrypt;
	
	DecryptStatus(byte[] bytes, boolean decrypted) {
		message = bytes;
		decrypt = decrypted;
	}
	
	byte[] getMessage() {
		return message;
	}
	
	boolean isDecryptedSuccessfully() {
		return decrypt;
	}
	
}
