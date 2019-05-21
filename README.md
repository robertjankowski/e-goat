[![CodeFactor](https://www.codefactor.io/repository/github/robertjankowski/e-goat/badge)](https://www.codefactor.io/repository/github/robertjankowski/e-goat)

# e-goat
P2P file transfer using UDP protocol


## Architecture of the program

1. Client logs to server which is always running (main thread for client)
2. Client who has logged to server runs second thread for listening to requests from server
3. Client in main thread select option
    - `GET_LIST_OF_FILES` 
        - client sends message to get all available files from all other clients
        - server receives message and asks each client for files (on client listen port)
        - server sends list of files to the client
    - `GET_FILES` (after client decides to choose which file to download)
        - client sends request to server with name of file and name of client who has this file
        - server asks client with file to send the file to the client who asked for it
        - clients start transferring files
    - `EXIT` 
        - terminate program 
  
<p align="center">
  <img title="Server diagram" src="images/e-goat-server.png" width="35%"/>
  <img title="Client diagram" src="images/e-goat-client.png" width="45%"/>
</p>

## Usage 
![example_usage](images/example.gif)


## Technology

- Java 11
- Maven

## Installation

```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk
sudo apt-get install maven
```

Check version
```bash
java -version

openjdk version "11.0.3" 2019-04-16
...
```
```bash
mvn -version

Apache Maven 3.6.0
...
```

## Build and run jar
Before specify the main class in `pom.xml`.
```bash
mvn clean install
mvn assembly:assembly
```
Then run as follows (both server and client)
```bash
java -jar `jar_file.jar` ip
```
Where `ip` could be `localhost` or wifi ip (in Windows `ipconfig`, Linux `ifconfig`).


### Authors
Robert Jankowski [@robertjankowski](https://github.com/robertjankowski)

Łukasz Bożek     [@xxlukasz11](https://github.com/xxlukasz11)
