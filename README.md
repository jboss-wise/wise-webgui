JBoss Wise GUI
==================================================================

System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on JBoss AS 7.1 or greater. 

Build and Deploy the application
--------------------------------

1. Make sure you have started the JBoss Server / WildFly server.
2. Type this command to build and deploy the archive:

        mvn clean package jboss-as:deploy (or mvn clean package wildfly:deploy)

3. This will deploy `target/wise-gui.war` to the running instance of the server.


Access the application 
---------------------

The application will be running at the following URL:  <http://localhost:8080/wise-gui/>.


Undeploy the Archive
--------------------

1. Make sure you have started the JBoss/ WildFly Server as described above.
2. When you are finished testing, type this command to undeploy the archive:

        mvn jboss-as:undeploy (or mvn wildfly:undeploy)
