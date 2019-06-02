package com.cyfrant.tgfrontend.utils;

import org.apache.tomcat.util.buf.HexUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {
    public static String sha256hex(String content) {
        byte[] utf = new byte[0];
        try {
            utf = content.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(utf);
            return HexUtils.toHexString(hash);
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}