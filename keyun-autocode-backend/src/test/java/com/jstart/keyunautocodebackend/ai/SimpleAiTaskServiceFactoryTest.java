package com.jstart.keyunautocodebackend.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SimpleAiTaskServiceFactoryTest {

    @Resource
    private SimpleAiTaskServiceFactory routingServiceFactory;

    @Test
    void aiCodeGenTypeRoutingService() throws InterruptedException {
        String[] prompts = {
                "做一个复杂的图书管理系统，包含功能有：图书添加、删除、修改、查询，用户注册、登录、借阅、归还等功能",
                "做一个简单的HTML页面",
                "做一个多页面网站项目"
        };
        // 使用虚拟线程并发执行
        Thread[] threads = new Thread[prompts.length];
        for (int i = 0; i < prompts.length; i++) {
            final String prompt = prompts[i];
            final int index = i + 1;
            threads[i] = Thread.ofVirtual().start(() -> {
                SimpleAiTaskService service = routingServiceFactory.aiCodeGenTypeRoutingService();
                var result = service.routeCodeGenType(prompt);
                log.info("线程 {}: {} -> {}", index, prompt, result.getValue());
            });
        }
        // 等待所有任务完成
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
