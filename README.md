# CS7IS3-Info-Retrieval main branch
instructions to download dataset

1. install pip and use pip to download gdown
sudo apt update
sudo apt install python3-pip -y
pip3 install gdown

2. use gdown to download dataset
gdown --id 17KpMCaE34eLvdiTINqj1lmxSBSu8BtDP

reference link:https://drive.google.com/file/d/17KpMCaE34eLvdiTINqj1lmxSBSu8BtDP/view

3. pip install unzip
sudo apt install unzip -y

4. unzip 'Assignment Two'.zip

### To run 

```
mvn package clean
java -jar target/CS7IS3_A2-1.1.jar
```

Or 

```
mvn clean compile
mvn exec:java -Dexec.mainClass="tcd.ie.luom.IndexProgram"
```

### To test TopicsParser

```
mvn clean compile
mvn exec:java -Dexec.mainClass="tcd.ie.luom.TestParser"
```

Notice: the structure explanation of the parser is written in the TopicsParser directory.
