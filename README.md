# Distributed Hash Table 

### High level design of DHT

![alt text](https://github.com/deepak-narkhede/distributed-systems/blob/main/DesignDraft.png?raw=true)

### Component Details
* **Client**

  * **Description** 
    * Client connects  to any of the chord member and performs operations using CRUD API's provided in client interface.
  * **Client Handle**
    * This handle is used by client api to communicate with DHT service.
  * **Response Handler**
    * This handler is used by client to fetch the response incoming from DHT service.
  
* **Chord Service**

  * **Description**
    * Responsible for cluster operations like joining or leaving a ring or creating a new ring.
    * Is is also main DHT processor process
  * **Chord Member**
    * It represents the chord node within the ring and contains all information about communications with other chord members.
    * It also holds metadata information like finger table, predecessor, successor etc. used by Chord protocol.
    * It is also responsible for stablization (fixing the finger table entries periodically).
    * Member ids range: (0 - 2^m -1) where m hash key length in bit
  * **Data Store Component**
    * This sub-component is responsible for data operations on chord ring.
    * Note: Currently only in-memory data store is implemented for storing the Pair<Key,Value>. 
    * It is extensible to implement any type of data store.
  * **Chord Server**
    * This sub-commponent is responsible for handling request from chord members.
    * It also handles request coming from client handle, based on the data owner the request is re-routed to appropriate chord member. Appropriate chord member is calucated using chord alforithm with help of successor and finger table using key.
  * **Finger Table**
    * It is used as part of chord protocol. 
    * Peer ith entry with id n : (n + 2^i)(mod 2^m)
  * **Ring Member Communication**
    * It is rmi interface used for communications between the chord members.  
    
* **Hashing Function**
  * Currently "SHA-1" hashing is used on key (For Data: data key, For Node: hostname:port). Hash key length is 160 bits.
  * It is flexible we can configure different hash function based on configuration.
  

* **In Progress**
 * Writing test cases.
 * Error handling.
