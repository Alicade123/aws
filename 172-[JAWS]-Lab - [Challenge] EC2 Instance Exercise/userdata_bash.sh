#!/bin/bash

dnf install -y httpd

systemctl enable httpd
systemctl start httpd

chmod 777 /var/www/html

echo "httpd installation completed successfully" > /var/log/ec2-challenge.log