FROM apache/skywalking-java-agent:8.12.0-java11

MAINTAINER zhoushuai zs11195310@163.com

ENV SW_AGENT_NAME cloud-rocketmq-exporter

ADD cloud-rocketmq-exporter.jar /app/cloud-rocketmq-exporter.jar

EXPOSE 5557

CMD java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/cloud-rocketmq-exporter.jar
