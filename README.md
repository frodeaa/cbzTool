# cbzTool

A collection of tools for downloading and converting CBZ files.

# Setup

Use maven to build the application, and include the *assembly:single* goal if you want to create a runnable JAR containing all dependencies.

	mvn clean install assembly:single

# Usage examples

**CBZ to PDF**
 
	java -jar cbztool-jar-with-dependencies.jar -pdf /path/comic.cbz /path/out.pdf
 