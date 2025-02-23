package com.itrihua.caritas;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


//排除分库分表
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAspectJAutoProxy
@EnableAsync
@EnableRedisHttpSession
@MapperScan("com.itrihua.caritas.mapper")
public class RihuaCaritasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RihuaCaritasBackendApplication.class, args);
    }

}
