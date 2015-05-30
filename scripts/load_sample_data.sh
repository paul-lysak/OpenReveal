#!/bin/sh
PROJ_DIR=/project
#this supposed to work, but it doesn't - "undefined method" Ruby exception is thrown
#./s-put http://localhost:3030/ds/data default $PROJ_DIR/src/test/resources/sample_data.turtle

#curl -v -X PUT -H "Content-Type: text/turtle" --data @$PROJ_DIR/src/test/resources/sample_data.turtle http://localhost:3030/ds/data?default
curl -v -X PUT -H "Content-Type: text/turtle" --upload-file $PROJ_DIR/src/test/resources/sample_data.turtle http://localhost:3030/ds/data?default
