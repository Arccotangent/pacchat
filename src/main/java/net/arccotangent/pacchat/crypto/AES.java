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

import net.arccotangent.pacchat.logging.Logger;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class AES {
	
	private static Logger aes_log = new Logger("CRYPTO/AES");
	
	static SecretKey generateAESKey() {
		try {
			aes_log.i("Generating 128 bit AES key.");
			
			KeyGenerator gen = KeyGenerator.getInstance("AES");
			gen.init(128);
			return gen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			aes_log.e("Error while generating AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] encryptBytes(byte[] toEncrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, aes);
			return c.doFinal(toEncrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while encrypting text with AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] decryptBytes(byte[] toDecrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, aes);
			return c.doFinal(toDecrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while decrypting text with AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
}
