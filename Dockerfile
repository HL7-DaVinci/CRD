FROM gradle:6.9.0-jdk11
 # Copy app files to container
 COPY --chown=gradle:gradle . /CRD/
 # Set working directory so that all subsequent command runs in this folder
 WORKDIR /CRD/server/
 # Embed CDS Library
 RUN gradle embedCdsLibrary
 # Expose port to access the app
 RUN gradle build
 EXPOSE 8090
 # Command to run our app
 CMD gradle bootRun 