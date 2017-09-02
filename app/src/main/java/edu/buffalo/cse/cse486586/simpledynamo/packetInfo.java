package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Tulika on 05-05-2017.
 */

public class packetInfo implements Serializable {
    private static final long serialVersionUID = 789456345456678789L;
    public String msgType;
    public String portId;
    public String portAdd;
    public boolean replicateFlag;
    public String contentKey;
    public String contentValue;
    public HashMap<String,String> globalStorage = new HashMap<String, String>();
    public HashMap<String,String> hmReplica = new HashMap<String, String>();
    public HashMap<String,String> hmCoord = new HashMap<String, String>();
    public String queryingNode;
    String coordPort;
    String crashedPort; // in case of failure, the failure node will call the next node and send the previous port no as a parameter to the next node.



    //insert
    public packetInfo(String contentKey, String contentValue, boolean replicateFlag, String portId){
        this.portId = portId;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.replicateFlag = replicateFlag;
    }
    //insertValues
    public packetInfo(String msgType, String contentKey, String contentValue, boolean replicateFlag, String portId){
        this.msgType = msgType;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.replicateFlag = replicateFlag;
        this.portId = portId;
    }


    public packetInfo(String msgType, String contentKey, String contentValue, boolean replicateFlag, String portId, String coordPort){
        this.msgType = msgType;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.replicateFlag = replicateFlag;
        this.portId = portId;
        this.coordPort = coordPort;
    }

    //queryAll
    public packetInfo(String msgType, HashMap<String, String> globalStorage,String queryingNode, String portId){
        this.msgType = msgType;
        this.globalStorage = globalStorage;
        this.queryingNode = queryingNode;
        this.portId = portId;
    }

    //querySpecific
    //("QuerySpecific", selection, specificHm, portStr, partitionNode.getNode(partitionNode))
    public  packetInfo(String msgType, String contentKey,  HashMap<String, String> globalStorage, String queryingNode, String portId ){
        this.msgType = msgType;
        this.contentKey = contentKey;
        this.globalStorage = globalStorage;
        this.queryingNode = queryingNode;
        this.portId = portId;
    }

    public  packetInfo(String msgType, String contentKey, String contentValue, HashMap<String, String> globalStorage, String queryingNode, String portId ){
        this.msgType = msgType;
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.globalStorage = globalStorage;
        this.queryingNode = queryingNode;
        this.portId = portId;
    }

    //deleteAll
    public packetInfo(String msgType, String queryingNode, String portId){
        this.msgType = msgType;
        this.queryingNode = queryingNode;
        this.portId = portId;
    }
    //deleteSpecific
    public packetInfo(String msgType, String contentKey, String queryingNode, String portId){
        this.msgType = msgType;
        this.contentKey = contentKey;
        this.queryingNode = queryingNode;
        this.portId = portId;
    }
    //RecoverAll
    public packetInfo(String msgType, HashMap<String, String> globalStorage, HashMap<String, String> hmCoord, HashMap<String, String> hmReplica, String queryingNode, String portId, String prevPort){
        this.msgType = msgType;
        this.globalStorage = globalStorage;
        this.queryingNode = queryingNode;
        this.portId = portId;
        this.crashedPort = prevPort;
        this.hmCoord = hmCoord;
        this.hmReplica = hmReplica;
    }
    public packetInfo(String msgType, HashMap<String, String> globalStorage, HashMap<String, String> hmCoord, HashMap<String, String> hmReplica, String queryingNode, String portId){
        this.msgType = msgType;
        this.globalStorage = globalStorage;
        this.queryingNode = queryingNode;
        this.portId = portId;
        this.hmCoord = hmCoord;
        this.hmReplica = hmReplica;
    }
}
