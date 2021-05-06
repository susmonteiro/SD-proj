# Application


## Authors

Group A24

### Lead developer 

92437 Catarina Gonçalves [@ist192437](https://git.rnl.tecnico.ulisboa.pt/ist192437) 
(commits might appear as 'root')


### Contributors


## About

This is a CLI (Command-Line Interface) application.


## Instructions for using Maven

To compile and run using _exec_ plugin:

```
mvn compile exec:java
```

To generate launch scripts for Windows and Linux
(the POM is configured to attach appassembler:assemble to the _install_ phase):

```
mvn install
```

To run using appassembler plugin on Linux:

```
./target/appassembler/bin/spotter ZooKeeper_IP ZooKeeper_Port username phoneNumber latitude longitude
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\spotter ZooKeeper_IP ZooKeeper_Port username phoneNumber latitude longitude
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

