FROM bellsoft/liberica-runtime-container:jdk-17-glibc
RUN apk add --no-cache msttcorefonts-installer fontconfig freetype
RUN update-ms-fonts --accept-eula
RUN mkdir -p /var/invoices/invoices
RUN mkdir -p /var/invoices/payslips
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app.jar"]
# ENTRYPOINT ["java", "-jar", "/app.jar"]