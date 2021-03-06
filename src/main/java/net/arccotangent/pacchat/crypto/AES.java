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
import java.security.NoSuchProviderException;

class AES {
	
	private static final Logger aes_log = new Logger("CRYPTO/AES");
	private static final int AES_bitsize = 128;
	
	static SecretKey generateAESKey() {
		try {
			aes_log.d("Generating " + AES_bitsize + " bit AES key.");
			
			KeyGenerator gen = KeyGenerator.getInstance("AES", "BC");
			gen.init(AES_bitsize);
			return gen.generateKey();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			aes_log.e("Error while generating AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] encryptBytes(byte[] toEncrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES", "BC");
			c.init(Cipher.ENCRYPT_MODE, aes);
			return c.doFinal(toEncrypt);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while encrypting text with AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static DecryptStatus decryptBytes(byte[] toDecrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES", "BC");
			c.init(Cipher.DECRYPT_MODE, aes);
			byte[] decrypted = c.doFinal(toDecrypt);
			return new DecryptStatus(decrypted, true);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while decrypting text with AES key!");
			e.printStackTrace();
		}
		return new DecryptStatus(null, false);
	}
	
}
