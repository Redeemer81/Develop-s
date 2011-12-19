@echo off
@title Login Server
set CLASSPATH=.;libs\*;dist\*;
java -Xmx100m -Dwzpath=wz\ -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd -Drecvops=recvops.properties -Dsendops=sendops.properties net.login.LoginServer
pause
