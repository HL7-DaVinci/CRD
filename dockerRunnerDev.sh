#!/bin/bash

 # Handle closing application on signal interrupt (ctrl + c)
 trap 'kill $CONTINUOUS_BUILD_PID $SERVER_PID; gradle --stop; exit' INT

 mkdir logs 
 # Reset log file content for new application boot
 echo "*** Logs for 'gradle installBootDist --continuous' ***" > ./logs/builder.log
 echo "*** Logs for 'gradle bootRun' ***" > ./logs/runner.log

 # Print that the application is starting in watch mode
 echo "starting application in watch mode..."

 # Start the continious build listener process
 echo "starting continuous build listener..."
 gradle build --continuous 2>&1 | tee ./logs/builder.log & CONTINUOUS_BUILD_PID=$!

 # Start server process once initial build finishes  
 ( while ! grep -m1 'BUILD SUCCESSFUL' < ./logs/builder.log; do
     sleep 1
 done
 echo "starting crd server in debug mode..."
 gradle bootRun -Pdebug 2>&1 | tee ./logs/runner.log ) & SERVER_PID=$!

 # Handle application background process exiting
 wait $CONTINUOUS_BUILD_PID $SERVER_PID
 EXIT_CODE=$?
 echo "application exited with exit code $EXIT_CODE..." 