#!/bin/bash

npm run-script build  # build the static files out

if [ -f ../static/js/main.index.js ]; then
    rm -rf ../static/js/main.index.js
    echo Deleted JS
fi

if [ -f ../static/main.index.css ]; then
    rm -rf ../static/main.index.css
    echo Deleted CSS
fi


if [ -f ./build/static/css/main.*.css ]; then
    mv ./build/static/css/main.*.css ../static/main.index.css
    echo Moved CSS
fi

if [ -f ./build/static/js/main.*.js ]; then
    mv ./build/static/js/main.*.js ../static/js/main.index.js
    echo Moved JS
fi
