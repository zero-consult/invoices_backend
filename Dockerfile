FROM bellsoft/liberica-runtime-container:jdk-17-glibc
RUN apk add --no-cache fontconfig freetype
RUN mkdir /var/invoices
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app.jar"]
# ENTRYPOINT ["java", "-jar", "/app.jar"]