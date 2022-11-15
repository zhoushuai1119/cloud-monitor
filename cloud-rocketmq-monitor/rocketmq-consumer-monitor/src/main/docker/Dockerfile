FROM apache/skywalking-java-agent:8.12.0-java11

MAINTAINER zhoushuai zs11195310@163.com

ENV SW_AGENT_NAME rocketmq-consumer-monitor

ADD rocketmq-consumer-monitor.jar /app/rocketmq-consumer-monitor.jar

EXPOSE 8077

CMD java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/rocketmq-consumer-monitor.jar
