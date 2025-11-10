package com.taobao;
// 测试修改一
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
/*
  @author Jimi
 */

@RestController
@EnableScheduling
@SpringBootApplication
@MapperScan("com.taobao.mapper")
public class MysqlConnectedApplication {
    public static void main(String[] args) {
        SpringApplication.run(MysqlConnectedApplication.class, args);

    }

    // 测试接口：检查 login.html 是否能被 Spring 读取到
    @GetMapping("/test-resource")
    public String testResource() {
        try {
            // 读取 classpath:/static/login.html（即 target/classes/static/login.html）
            ClassPathResource resource = new ClassPathResource("static/login.html");
            if (resource.exists()) {
                return "✅ 静态资源存在！路径：" + resource.getFile().getAbsolutePath();
            } else {
                return "❌ 静态资源不存在！";
            }
        } catch (IOException e) {
            return "❌ 读取资源出错：" + e.getMessage();
        }
    }
}

/*
  MVC三层架构
   mapper层 和数据库建立连接 拉取和写回数据
   conteoller层 接受用户请求 返回响应
   service层 处理业务逻辑
 */