## Logging Framework Used Guide

###step 1: 
````


引入依赖 : compile('com.**.framework:logging-starter:1.0.2-RELEASE')


````

###step 2:  
````

@Import({LoggingAutoConfiguration.class})
//pre request with (RequestId && TrackeId) in header

````
> 可在任意带有@Configuration 注解的类上引入(或者bean类), 推荐在Configuration类上面导入
eg: 
```java
@Configuration
@Import({ LoggingAutoConfiguration.class})
public class NotificationAdminConfiguration {
}
```

###step 3：
```` 
配置项:

//need application name(配置项目名称)
spring.application.name=your-application-api

//logging(打印日志需要配置该项)
logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=debug
logging.level.com.**.framework.logging.interceptor.RequestLoggingInterceptor=debug
logging.level.org.springframework.web.servlet.DispatcherServlet=DEBUG
// logging color
spring.output.ansi.enabled=detect

//sleuth(可选，如果需要 sleuth和zipkin 可以加上以下的配置)
spring.sleuth.sampler.probability=1
spring.zipkin.locator.discovery.enabled=true
spring.zipkin.baseUrl=http://127.0.0.1:9411/
spring.zipkin.sender.type=web
//开启支持opentracing
spring.sleuth.opentracing.enabled=true

````

###step 4: 

#####引用logging-starter 的项目要创建 sentry.properties文件
>文件内容
````$xslt
dsn=https://c13b5f3f56b441518325d31177922101:c97ea0a1fecf45eca8d7482ce3a86ac8@sentry.dev.**.net/
sample.rate=1
#mdctags=foo,bar
````

#####或者使用vm参数引用sentry  (在启动配置里加上该参数)
// starup used param for sentry
// sentry
java -Dsentry.dsn=https://public:private@host:port/1 -jar app.jar


#### JMS Tracing

1. import jms  jar with gradle

````
compile("com.**.framework.jms:starter-jms-amazon:${**FrameworkJmsVersion}") { changing = true }
````
2.  in @Configuration import TracingJmsAutoConfiguration.class

````
@Improt(TracingJmsAutoConfiguration.class)
````

#### Database Tracing

1.  add  ?statementInterceptors=brave.mysql.TracingStatementInterceptor&useSSL=false  in database url

````
````
spring.datasource.url=jdbc:mysql://**-study.alpha.mysql.**.net:3306/notification?statementInterceptors=brave.mysql.TracingStatementInterceptor&useSSL=false
````




