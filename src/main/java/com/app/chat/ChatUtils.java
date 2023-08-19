package com.app.chat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class ChatUtils {

    private static final String PASSWORD = "cryptoisfun";
    private static final String SALT = "cryptoisbun";



    public static String encryptPasswordBased(String plainText)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,getKeyFromPassword(), generateIv());
        return Base64.getEncoder()
                .encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    public static String decryptPasswordBased(String cipherText)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(), generateIv());
        return new String(cipher.doFinal(Base64.getDecoder()
                .decode(cipherText)));
    }

    public static SecretKey getKeyFromPassword()
            throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(PASSWORD.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public static PublicPrivateKeyPair getPublicPrivateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();
        return new PublicPrivateKeyPair(Base64.getEncoder().encodeToString(pub.getEncoded()), Base64.getEncoder().encodeToString(pvt.getEncoded()));

    }

    public static IvParameterSpec generateIv() throws IOException {
        byte[] iv = new byte[16];
        File f = new File("iv");
        if (f.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String b = br.readLine();
            return new IvParameterSpec(Base64.getDecoder().decode(b));
        } else {
            new SecureRandom().nextBytes(iv);
            FileWriter fw = new FileWriter("iv");
            fw.write(Base64.getEncoder().encodeToString(iv));
            fw.flush();
            fw.close();
            return new IvParameterSpec(iv);
        }
    }

    public static class PublicPrivateKeyPair {
        String publicKey;
        String privateKey;

        public PublicPrivateKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    }

    // Decrypt using RSA public key
    public static String decryptMessage(String encryptedText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
    }

    // Encrypt using RSA private key
    public static String encryptMessage(String plainText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
    }

    public static void main(String[] args) throws Exception {
        ChatUtils.PublicPrivateKeyPair publicPrivateKeys = ChatUtils.getPublicPrivateKeys();
        String s = ChatUtils.encryptPasswordBased(publicPrivateKeys.getPrivateKey());
        String privateKeyStr = ChatUtils.decryptPasswordBased(s);
        System.out.println(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);

    }

}
