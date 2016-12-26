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

package net.arccotangent.pacchat.filesystem;

import net.arccotangent.pacchat.crypto.RSA;
import net.arccotangent.pacchat.logging.Logger;
import net.arccotangent.pacchat.net.Server;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {
	
	private static final Logger km_log = new Logger("FILESYSTEM/KEYMANAGER");
	private static final String user_home = System.getProperty("user.home");
	private static final String installationPath = user_home + File.separator + ".pacchat";
	private static final File installationFile = new File(installationPath);
	
	private static final String ANSI_BOLD = "\u001B[1m";
	private static final String ANSI_WHITE = "\u001B[37m";
	private static final String ANSI_RESET = "\u001B[0m";
	
	private static final File privkeyFile = new File(installationPath + File.separator + "local.priv");
	private static final File pubkeyFile = new File(installationPath + File.separator + "local.pub");
	
	private static String PBE_algorithm = "PBEWithSHA1AndDES";
	private static final int PBE_iterations = 160000;
	private static final int RSA_bitsize = 4096;
	
	private static byte[] generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return salt;
	}
	
	public static boolean keysEncrypted() {
		try {
			String privFileContents = new String(Files.readAllBytes(privkeyFile.toPath()));
			return privFileContents.split("\n")[0].equals("1");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	private static boolean isEncrypted(String key) {
		return key.split("\n")[0].equals("1");
	}
	*/
	
	public static String encryptPrivkey(String privkeyB64, char[] password) {
		km_log.d("Encrypting private key.");
		km_log.d("PBE Algorithm = " + PBE_algorithm);
		km_log.d("PBE iteration count = " + PBE_iterations);
		try {
			byte[] salt = generateSalt();
			PBEKeySpec keySpec = new PBEKeySpec(password, salt, PBE_iterations);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_algorithm, "BC");
			SecretKey key = keyFactory.generateSecret(keySpec);
			
			Cipher cipher = Cipher.getInstance(PBE_algorithm, "BC");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedPrivkey = cipher.doFinal(Base64.decodeBase64(privkeyB64));
			
			return "1\n" + Base64.encodeBase64String(salt) + "\n" + Base64.encodeBase64String(encryptedPrivkey);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			km_log.e("Error encrypting private key!");
			e.printStackTrace();
		}
		return privkeyB64;
	}
	
	public static String decryptPrivkey(String encryptedB64, char[] password) {
		km_log.d("Decrypting private key.");
		km_log.d("PBE Algorithm = " + PBE_algorithm);
		km_log.d("PBE iteration count = " + PBE_iterations);
		String[] keyParts = encryptedB64.split("\n");
		//keyParts[0] = 1 (indicates encrypted private key)
		//keyParts[1] = base 64 8 byte salt for PBE
		//keyParts[2] = encrypted base 64 private key
		
		byte[] salt = Base64.decodeBase64(keyParts[1]);
		byte[] encryptedPrivkey = Base64.decodeBase64(keyParts[2]);
		
		for (int i = 0; i < keyParts.length; i++) {
			km_log.d("keyParts[" + i + "] = " + keyParts[i]);
		}
		
		try {
			PBEKeySpec keySpec = new PBEKeySpec(password, salt, PBE_iterations);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_algorithm, "BC");
			SecretKey key = keyFactory.generateSecret(keySpec);
			
			Cipher cipher = Cipher.getInstance(PBE_algorithm, "BC");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			byte[] privkey = cipher.doFinal(encryptedPrivkey);
			
			return Base64.encodeBase64String(privkey);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			km_log.e("Error decrypting private key!");
			e.printStackTrace();
			if (e instanceof BadPaddingException) {
				km_log.e("Bad decryption password?");
			}
		}
		return null;
	}
	
	private static void generateNewKeys() {
		km_log.i("Generating new keys.");
		km_log.w("-----------------------------------------------------------------------");
		km_log.w("FOR YOUR SECURITY, PLEASE ENCRYPT YOUR KEYS!");
		km_log.w("Use the 'encrypt' command to encrypt your keys after generation is complete!");
		km_log.w("-----------------------------------------------------------------------");
		KeyPair keyPair = RSA.generateRSAKeypair(RSA_bitsize);
		km_log.d(RSA_bitsize + " bit RSA key generated.");
		
		assert keyPair != null;
		RSAPrivateKey privkey = (RSAPrivateKey) keyPair.getPrivate();
		RSAPublicKey pubkey = (RSAPublicKey) keyPair.getPublic();
		
		saveUnencryptedKeys(privkey, pubkey);
	}
	
	public static void saveEncryptedKeys(String privkeyCryptB64, RSAPublicKey pubkey) {
		km_log.i("Saving encrypted keys to disk.");
		
		km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
		km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
		
		try {
			if (!pubkeyFile.exists())
				km_log.d(pubkeyFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubkeyFile);
			pubOut.write(Base64.encodeBase64(pubkey.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key!");
			e.printStackTrace();
		}
		
		try {
			if (!privkeyFile.exists())
				km_log.d(privkeyFile.createNewFile() ? "Creation of private key file successful." : "Creation of private key file failed!");
			
			FileOutputStream privOut = new FileOutputStream(privkeyFile);
			privOut.write(privkeyCryptB64.getBytes());
			privOut.flush();
			privOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving private key!");
			e.printStackTrace();
		}
		
		km_log.i("Finished saving keys to disk. Operation appears successful.");
	}
	
	public static void saveUnencryptedKeys(RSAPrivateKey privkey, RSAPublicKey pubkey) {
		km_log.i("Saving keys to disk.");
		
		km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
		km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
		
		try {
			if (!pubkeyFile.exists())
				km_log.d(pubkeyFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubkeyFile);
			pubOut.write(Base64.encodeBase64(pubkey.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key!");
			e.printStackTrace();
		}
		
		try {
			if (!privkeyFile.exists())
				km_log.d(privkeyFile.createNewFile() ? "Creation of private key file successful." : "Creation of private key file failed!");
			
			FileOutputStream privOut = new FileOutputStream(privkeyFile);
			privOut.write(Base64.encodeBase64(privkey.getEncoded()));
			privOut.flush();
			privOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving private key!");
			e.printStackTrace();
		}
		
		km_log.i("Finished saving keys to disk. Operation appears successful.");
	}
	
	public static String loadCryptedPrivkey() {
		try {
			km_log.i("Loading encrypted RSA private key from disk.");
			km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
			byte[] privEncoded = Files.readAllBytes(privkeyFile.toPath());
			
			return new String(privEncoded);
		} catch (IOException e) {
			km_log.e("Error while loading private key!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static RSAPrivateKey loadUnencryptedPrivkey() {
		try {
			km_log.i("Loading unencrypted RSA private key from disk.");
			km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
			km_log.d("Private key file = " + privkeyFile.getAbsolutePath());
			byte[] privEncoded = Files.readAllBytes(privkeyFile.toPath());
			
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privEncoded));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			
			return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			km_log.e("Error while loading private key!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static RSAPrivateKey Base64ToPrivkey(String b64Privkey) {
		try {
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(b64Privkey));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			
			return (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			km_log.e("Error while decoding base 64 private key!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static RSAPublicKey loadPubkey() {
		try {
			km_log.i("Loading RSA public key from disk.");
			km_log.d("Public key file = " + pubkeyFile.getAbsolutePath());
			byte[] pubEncoded = Files.readAllBytes(pubkeyFile.toPath());
			
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decodeBase64(pubEncoded));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			
			return (RSAPublicKey) keyFactory.generatePublic(pubSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			km_log.e("Error while loading public key!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static RSAPublicKey loadKeyByIP(String ip_address) {
		km_log.i("Loading public key for " + ip_address);
		try {
			File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
			km_log.d("Key file = " + pubFile.getAbsolutePath());
			
			byte[] pubEncoded = Files.readAllBytes(pubFile.toPath());
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decodeBase64(pubEncoded));
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			return (RSAPublicKey) keyFactory.generatePublic(pubSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			km_log.e("Error while loading public key for " + ip_address + "!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean checkIfIPKeyExists(String ip_address) {
		File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
		return pubFile.exists();
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void saveKeyByIP(String ip_address, RSAPublicKey publicKey) {
		km_log.i("Saving public key for " + ip_address);
		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKey.getEncoded());
		File pubFile = new File(installationPath + File.separator + ip_address + ".pub");
		km_log.d("Key file = " + pubFile.getAbsolutePath());
		
		km_log.i("Deleting old key if it exists.");
		if (pubFile.exists())
			pubFile.delete();
		
		try {
			km_log.i(pubFile.createNewFile() ? "Creation of public key file successful." : "Creation of public key file failed!");
			
			FileOutputStream pubOut = new FileOutputStream(pubFile);
			pubOut.write(Base64.encodeBase64(pubSpec.getEncoded()));
			pubOut.flush();
			pubOut.close();
			
		} catch (IOException e) {
			km_log.e("Error while saving public key for " + ip_address + "!");
			e.printStackTrace();
		}
	}
	
	public static void createInstallationIfNotExist() {
		km_log.i("Checking if installation exists...");
		if (checkInstallation()) {
			km_log.i("Installation exists. Checking for keys...");
			
			if (checkLocalKeys()) {
				km_log.i("Keys exist. No further action required.");
			} else {
				km_log.e("One or both keys do not exist. Checking to see which key.");
				if (!privkeyFile.exists()) {
					km_log.e("Private key does not exist. Generating new keys is necessary.");
					deleteLocalKeysIfExist();
					generateNewKeys();
				} else if (!pubkeyFile.exists()) {
					km_log.e("Public key does not exist. Attempting recovery from private key.");
					RSAPrivateKey privkey;
					char[] password;
					if (keysEncrypted()) {
						km_log.i("Private key is encrypted!");
						km_log.i("Please enter password to decrypt. The password will not be shown.");
						km_log.w("The decrypted private key will not be stored as it normally is. You will need to enter your password again. Your keys will not be stored to disk unencrypted at any time during the recovery.");
						System.out.print(ANSI_BOLD + ANSI_WHITE + "Decryption password: " + ANSI_RESET);
						password = System.console().readPassword();
						km_log.i("Attempting decryption.");
						String b64Privkey_crypt = loadCryptedPrivkey();
						String b64Privkey = decryptPrivkey(b64Privkey_crypt, password);
						if (b64Privkey == null) {
							km_log.e("Decrypted private key is null. If you do not want to recover your public key, you must delete the private key manually and restart PacChat.");
							km_log.e("When PacChat is restarted, new keys will be generated for you.");
							System.exit(1);
						}
						privkey = Base64ToPrivkey(b64Privkey);
						RSAPublicKey recoveredPubkey = recoverPubkey(privkey);
						saveEncryptedKeys(b64Privkey_crypt, recoveredPubkey);
					} else {
						privkey = loadUnencryptedPrivkey();
						RSAPublicKey recoveredPubkey = recoverPubkey(privkey);
						saveUnencryptedKeys(privkey, recoveredPubkey);
					}
					password = null;
				}
			}
		} else {
			km_log.w("Installation does not exist. Creating new installation now.");
			boolean mkdirSuccessful = installationFile.mkdir();
			if (mkdirSuccessful) {
				km_log.i("Installation creation successful. Generating new keys.");
				generateNewKeys();
			} else {
				km_log.e("Installation creation failed! Could not create directory.");
			}
		}
	}
	
	private static RSAPublicKey recoverPubkey(RSAPrivateKey privateKey) {
		RSAPrivateCrtKey certKey = (RSAPrivateCrtKey) privateKey;
		BigInteger pubExponent = certKey.getPublicExponent();
		BigInteger modulus = certKey.getModulus();
		
		RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modulus, pubExponent);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			return (RSAPublicKey) keyFactory.generatePublic(pubSpec);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
			km_log.e("Error while recovering public key from private key!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static RSAPublicKey downloadKeyFromIP(String ip_address) {
		try {
			km_log.i("Downloading key from IP " + ip_address);
			Socket socketGetkey = new Socket();
			socketGetkey.connect(new InetSocketAddress(InetAddress.getByName(ip_address), Server.PORT), 1000);
			BufferedReader inputGetkey = new BufferedReader(new InputStreamReader(socketGetkey.getInputStream()));
			BufferedWriter outputGetkey = new BufferedWriter(new OutputStreamWriter(socketGetkey.getOutputStream()));
			
			outputGetkey.write("301 getkey");
			outputGetkey.newLine();
			outputGetkey.flush();
			
			String pubkeyB64 = inputGetkey.readLine();
			byte[] pubEncoded = Base64.decodeBase64(pubkeyB64);
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			
			outputGetkey.close();
			inputGetkey.close();
			
			return (RSAPublicKey) keyFactory.generatePublic(pubSpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			km_log.e("Error saving recipient's key!");
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean checkInstallation() {
		return installationFile.exists();
	}
	
	private static boolean checkLocalKeys() {
		return (privkeyFile.exists() && pubkeyFile.exists());
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void deleteLocalKeysIfExist() {
		if (pubkeyFile.exists())
			pubkeyFile.delete();
		if (privkeyFile.exists())
			privkeyFile.delete();
	}
	
	public static String fingerprint(RSAPublicKey pubkey) {
		byte[] keyBytes = pubkey.getEncoded();
		try {
			MessageDigest hasher = MessageDigest.getInstance("SHA3-512", "BC");
			hasher.update(keyBytes);
			return Hex.encodeHexString(hasher.digest());
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			km_log.e("Error computing key fingerprint!");
			e.printStackTrace();
		}
		return null;
	}
	
}
