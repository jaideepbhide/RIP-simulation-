# RIP Simulation

Name: Jaideep Bhide
RIT_id = 809004353

Build/Run Instructions

The zip file contains one README.txt file and a Rover.java file.

To a network topology upload the Rover.java file onto multiple machines 
that are used.
Compile the Rover.java file using javac Rover.java

1 JAVA FILE CORRESPONDS TO 1 ROVER OR BASE STATION

To RUN the file provide 3 command line arguments 
1) multicast Address
2) port
3) rover id ( this value will be different on different machines/VMs)

Example RUN statement :   java Rover 224.0.0.0 520 1


The same rover file should be duplicated (copy/paste) to create 11 different 
rovers , 1 base station and 10 rovers.
Example:

Virtual Machine 1
java Rover 224.0.0.0 520 0   ---- (Base Station)
Virtual Machine 2
java Rover 224.0.0.0 520 1   ---- (1st Rover)
Virtual Machine 2
java Rover 224.0.0.0 520 2   ---- (2nd Rover)
Virtual Machine 3
java Rover 224.0.0.0 520 3   ---- (3rd Rover)
