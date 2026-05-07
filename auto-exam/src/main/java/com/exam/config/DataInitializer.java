package com.exam.config;

import com.exam.entity.Admin;
import com.exam.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化：创建默认管理员账号
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!adminRepository.existsByUsername("xyf")) {
            Admin admin = new Admin();
            admin.setUsername("xyf");
            admin.setPassword(passwordEncoder.encode("xyf123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@exam.com");
            admin.setRole(0);
            adminRepository.save(admin);
            System.out.println("========================================");
            System.out.println("  默认管理员账号已创建");
            System.out.println("  用户名: xyf");
            System.out.println("  密  码: xyf123");
            System.out.println("========================================");
        }
    }
}
