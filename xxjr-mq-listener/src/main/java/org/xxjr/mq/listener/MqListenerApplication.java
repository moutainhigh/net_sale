package org.xxjr.mq.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.xxjr.mq.listener.util.AnnotationUtil;
import org.xxjr.mq.listener.util.XxjrInitAnnotation;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@ComponentScan(basePackages = {"org.xxjr.*","org.ddq.*","org.llw.*"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
public class MqListenerApplication {
	public static void main(String[] args) {
		log.info("=============xxjr-mq-listener is run start===============");
		SpringApplication.run(MqListenerApplication.class, args);
		log.info("xxjr-mq-listener is run success!!!!!!!!!!!!!");
		
		log.info("execXxjrAnnotationInit start!!!!!!!!!!!!!");
		// 初始化消费者方法
		AnnotationUtil.execXxjrAnnotationInit(XxjrInitAnnotation.class);
		log.info("execXxjrAnnotationInit end!!!!!!!!!!!!!");
	}
}

