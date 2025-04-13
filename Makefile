jar:
	javac BusyboxLauncher.java
	jar cfm BusyboxLauncher.jar MANIFEST.MF BusyboxLauncher.class
clean:
	rm BusyboxLauncher.class
	rm BusyboxLauncher.jar
