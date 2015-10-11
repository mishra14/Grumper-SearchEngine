ant build
sudo /etc/init.d/jetty stop
sudo cp servlet.war /usr/share/jetty/webapps
sudo /etc/init.d/jetty start

