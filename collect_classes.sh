#!/bin/bash

BINDIR=bin
ZIPFILE=bin.zip

# clean up from previous collections
rm -rf $BINDIR
rm $ZIPFILE

# mkdir the directory for the bins
mkdir $BINDIR

# find all of the proper folders with classes built with gradle from the command line
FOLDERS=`find . -name 'classes' | grep build | egrep -v 'reports'`

# loop through the found folders with classes
for FOLDER in $FOLDERS; do

  # create the output folder
  OUT_FOLDER=$BINDIR/$FOLDER
  echo "creating output folder: $OUT_FOLDER"
  mkdir -p $OUT_FOLDER

  #CLASSES=`find $FOLDER -name '*.class' -type f`
  #echo $CLASSES

  # copy all of the files within the class folders to the output
  echo "copying all of the files from $FOLDER to $OUT_FOLDER"
  cp -rf $FOLDER/* $OUT_FOLDER

done

# create the zip file
zip -r $ZIPFILE $BINDIR

# delete the collected bins
rm -rf $BINDIR

echo "All .class files collected into $ZIPFILE"

