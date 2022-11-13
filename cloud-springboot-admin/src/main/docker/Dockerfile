FROM apache/skywalking-java-agent:8.12.0-java11

MAINTAINER zhoushuai zs11195310@163.com

ENV SW_AGENT_NAME cloud-springboot-admin

ADD cloud-springboot-admin.jar /app/cloud-springboot-admin.jar

EXPOSE 8013

CMD java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/cloud-springboot-admin.jar
