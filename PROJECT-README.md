# CS441 - Course Project

---
# Project title :  To create and evaluate Chord, a cloud overlay network algorithm with consistent hashing using your own Akka/HTTP-based simulator.

---
### Project members
* Abhijeet Mohanty
* Aditya Sawant
* Saurabh Vijay Singh
* Kewal Shah

### Development Environment
* **Development environments used** : Windows 10, MacOS Mojave
* **Framework used** :  Akka 2.6.0 and Akka-HTTP 10.1.11
* **IDE Used** : IntelliJ IDEA 2018.1.8, IntelliJ IDEA 2019.1.2
* **Build tools used** : SBT 1.1.2

### Overview
* Chord is a peer to peer algorithm which determines a distributed hash table. Such a hash table stores keys across various nodes in the aforementioned P2P network.

### Steps to run
* Clone the git repository - **abhijeet_mohanty_cs441_course_project** and navigate into it.
* Run the following command - `sbt clean compile test run`
* The console log generated would give a link on which the web service would be booted at `http://localhost:8080`

### Docker
* The docker image of our simulator is hosted on dockerhub with id: saurabhvijaysingh/cs_441_course_project_chord:submission
* To pull the docker image to local- `docker pull saurabhvijaysingh/cs_441_course_project_chord:submission`
* To run the docker image - `docker run -t -p 8080:8080 --name cs441-chord-container saurabhvijaysingh/cs_441_course_project_chord:submission`

### Features and code flow

* Adding a node to a ring
    - In this operation, we add a server to a ring. This is followed by a step where the finger table corresponding to each server is updated to take the addition into account.
    - In this case, we add some data - here it is a unit of data which corresponds to a movie. Here the key corresponding to the file is hashed and this file is loaded onto a server which corresponds to a key whose hash is the smallest value 
* Lookup of a data in some node
    - `ServerActor`
        * This class represents an Akka actor which corresponds to a server in the chord ring.
        * The messages defined on this actor are as follows
            * `InitFingerTable`
                * This case class corresponds to a message which initializes the finger table of the server.
            * `LoadData`
                * This case class corresponds to a message to a load a unit of data onto a server.
            * `GetData`
                * This case class is a message to lookup for a data using the chord algorithm.
    - `UserActor`
        * This actor corresponds to the user whose functions include adding a unit of data and the looking it up.
        * `CreateUserActorWithId`
            * This case class corresponds to a message which initializes a particular instance of a user actor.
        * `AddFileToServer`
            * Through this message, an instance of a user actor loads a unit of data onto some server.
        * `LookUpData`
            * Through this message, the user is able to send some unit of data to be loaded onto some server.
* Loading data to a node in the ring
    * In this operation, we add some data - here it is a unit of data which corresponds to a movie. Here the key corresponding to the file is hashed and this file is loaded onto a server which corresponds to a key whose hash is the smallest value.
* Lookup of a data in some node 
    * In this operation, the request for the lookup for a particular unit of data is routed to a particular node. The finger table of the node is referred to and the hash which is the smallest value such that it is greater than or equal to the hash corresponding to the hash of the file is chosen. If no such hash exists then we forward the request to a node which is its successor.
* Web services
    * We make use of the Akka-HTTP libraries for creating multiple routes for handling the said features in our simulator. This is our entry point to the program. By hitting the URLs with appropriate parameters (given below) we are calling the backend methods for above mentioned tasks.
    * The `WebService` object denotes the entry point to the application where is runs on `localhost:8080`
    * When the application is started, the web applications displays 4 links :
    * `addNode`
        *   In this method we add a node to a ring.
    * `loadData`
        * A unit of data is loaded onto a server node  by the user.
        * Append the request with `?id=<some positive integer>`
    * `lookUpData`
        * A unit of data is looked up by a user on a server node using the Chord algorithm for lookup.
        * Append the request with `?id=<some positive integer>`   
    * `createSnapshot`
        * The snapshot which comprises of information such as total no. of server nodes in the ring, finger table of each server and the data loaded onto each server is displayed.
    * The service which runs on `http://localhost:8080` displays 4 hyperlinks corresponding to the above methods respectively. 
* `HashUtils`
    *  This class generates a hash of a certain no. of bits using the **SHA-1** hashing algorithm. 
    *  We make use of this util to generate keys for our server nodes and data units.

### Results

* Adding a server node to the ring repeatedly, we get the following data :
```
11:13:15.542 [actor-system-akka.actor.default-dispatcher-19] INFO com.akka.server.ServerActor - Created server with path akka://actor-system/user/server-actor-supervisor/server-actor-4
11:13:17.221 [actor-system-akka.actor.default-dispatcher-17] INFO com.akka.server.ServerActor - Created server with path akka://actor-system/user/server-actor-supervisor/server-actor-5
```
* Adding data to a node to some server repeatedly :
```
11:12:32.174 [actor-system-akka.actor.default-dispatcher-14] INFO com.akka.server.ServerActor - Loaded data Data(3,What Happens in Vegas) in server with path akka://actor-system/user/server-actor-supervisor/server-actor-0
11:17:14.840 [actor-system-akka.actor.default-dispatcher-23] INFO com.akka.server.ServerActor - Loaded data Data(7,Waiting For Forever) in server with path akka://actor-system/user/server-actor-supervisor/server-actor-5
```

* Look up of data
```
11:21:11.657 [actor-system-akka.actor.default-dispatcher-33] INFO com.akka.master.MasterActor - Querying data Data(33,Nick and Norah's Infinite Playlist) from server
11:21:11.669 [actor-system-akka.actor.default-dispatcher-34] INFO com.akka.server.ServerActor - Found data Data(33,Nick and Norah's Infinite Playlist) in server akka://actor-system/user/server-actor-supervisor/server-actor-6
```

* Get Snapshot
```
ListBuffer(Server : akka://actor-system/user/server-actor-supervisor/server-actor-7 with finger table : Map(8 -> FingerTableEntry(18709,18453), 11 -> FingerTableEntry(20501,18453), 2 -> FingerTableEntry(18457,18453), 5 -> FingerTableEntry(18485,18453), 14 -> FingerTableEntry(2069,18453), 13 -> FingerTableEntry(26645,18453), 4 -> FingerTableEntry(18469,18453), 7 -> FingerTableEntry(18581,18453), 1 -> FingerTableEntry(18455,18453), 10 -> FingerTableEntry(19477,18453), 9 -> FingerTableEntry(18965,18453), 3 -> FingerTableEntry(18461,18453), 12 -> FingerTableEntry(22549,18453), 6 -> FingerTableEntry(18517,18453), 0 -> FingerTableEntry(18454,18453)) and movie list : ListBuffer(Data(1,You Will Meet a Tall Dark Stranger)))
```
### Future improvements 
* To simulate a load testing scenario where the no. of users, no. of servers and the amount of data is randomized. This along with scheduling failures of server nodes, read/ write ratio of each user etc.


