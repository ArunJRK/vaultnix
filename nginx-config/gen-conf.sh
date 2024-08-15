#!/bin/bash

# Create conf files for each Nginx server
for i in {1..3}
do
    sed "s/SERVER_NAME/nginx$i.example.com/g" nginx-conf-template > nginx$i.conf
done

echo "Nginx configuration files created successfully!"