package com.yourcompany.erp.common.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类（使用 SHA-256 + Salt）
 * 注意：生产环境建议使用 BCrypt
 */
@Component
public class PasswordUtils {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * 生成随机盐值
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码（格式：salt$hash）
     */
    public String encode(String rawPassword) {
        try {
            String salt = generateSalt();
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hash);
            return salt + "$" + hashString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            String salt = parts[0];
            String originalHash = parts[1];

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hash);

            return hashString.equals(originalHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码验证失败", e);
        }
    }

}
