FROM gradle:jdk8-alpine
EXPOSE 8090/tcp
COPY --chown=gradle:gradle . /CRD/
WORKDIR /CRD/server/
RUN gradle build
CMD ["gradle", "bootRun"]
