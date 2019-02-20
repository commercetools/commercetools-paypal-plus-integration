FROM java:openjdk-8-jre

MAINTAINER commercetools Professional Services <ps-dev@commercetools.de>

COPY build/libs/commercetools-paypalplus-integration.jar /app/commercetools-paypalplus-integration.jar

CMD ["java", "-jar", "/app/commercetools-paypalplus-integration.jar"]

# verify every 30 seconds CommercetoolsHealthController returns status code 200
HEALTHCHECK --start-period=10s \
            --interval=30s \
            --timeout=5s \
            --retries=3 \
            CMD (test $( \
                        curl http://localhost:8080/health \
                            --head \
                            --silent \
                            --write-out %{http_code} \
                            --output /dev/null) \
                        -eq 200) \
                        || exit 1

EXPOSE 8080
