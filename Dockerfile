FROM java:openjdk-8-jre

MAINTAINER commercetools Professional Services <ps-dev@commercetools.de>

COPY build/libs/commercetools-paypalplus-integration.jar /app/commercetools-paypalplus-integration.jar

CMD ["java", "-jar", "/app/commercetools-paypalplus-integration.jar"]

EXPOSE 8080
