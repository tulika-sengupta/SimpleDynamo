//// FINAL BACKUP
//
//package edu.buffalo.cse.cse486586.simpledynamo;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Formatter;
//import java.util.HashMap;
//
//import android.content.ContentProvider;
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.MatrixCursor;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//
//import org.w3c.dom.Node;
//
//public class SimpleDynamoProvider extends ContentProvider {
//
//    public static final String TAG = SimpleDynamoProvider.class.getSimpleName();
//    public static final String REMOTE_PORT_ADD[] = {"11108","11112","11116","11120","11124"};
//    public static final String REMOTE_PORT_ID[] = {"5554","5556","5558","5560","5562"};
//    public static final HashMap<String, String> PortId_Add_Map = new HashMap<String, String>();
//    public static final HashMap<String, String> PortId_Hash_Map = new HashMap<String, String>();
//
//    public static HashMap<String, String> hm = new HashMap<String, String>();   //local storage
//    public static HashMap<String, String> hmReplica = new HashMap<String, String>();
//    public static HashMap<String, String> hmCoord = new HashMap<String, String>();
//
//    public static HashMap<String, String> globalHm = new HashMap<String, String>();   //local storage
//    public static HashMap<String, String> globalQueryHm = new HashMap<String, String>();   //local storage
//    public static HashMap<String, String> specificHm = new HashMap<String, String>();
//    public static HashMap<String, String> recoverHm = new HashMap<String, String>();
//
//
//    private static final String KEY_FIELD = "key";
//    private static final String VALUE_FIELD = "value";
//    public String portStr;
//    public String myPort;
//    public static boolean waitStatus;
//    public static boolean specQueryWait;
//    public static boolean queryAllWait;
//    public static boolean delWait;
//    public String queryValue;
//    NodeInsertion ni = new NodeInsertion();
//    public int recoveredInsert=1;
//    public int recoveredQuery=1;
//
//    public static String portOrder[];
//
//
//
//    public static final int SERVER_PORT = 10000;
//
//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        // TODO Auto-generated method stub
//        if(selection.compareTo("@")==0){
//            for(String keys: hm.keySet()){
//                hm.remove(keys);
//            }
//        }
//        else if(selection.compareTo("*")==0){
//            for(String keys: hm.keySet())
//            {
//                hm.remove(keys);
//            }
//            NodeInsertion fNode = ni.findNode(portStr);
//            packetInfo pDel = new packetInfo("DeleteAll", portStr, fNode.getNext(fNode).getNode(fNode.getNext(fNode)));
//            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pDel);
//        }
//
//        else{
//            getPartition("DeleteSpecific", selection, "", portStr);
//        }
//        return 0;
//    }
//
//    public void deleteAll(packetInfo p){
//        for(String keys: hm.keySet())
//        {
//            hm.remove(keys);
//        }
//        NodeInsertion temp = ni.findNode(p.portId);
//        temp = temp.getNext(temp);
//        packetInfo pDel = new packetInfo("DeleteAll",p.queryingNode,temp.getNode(temp));
//        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pDel);
//
//    }
//    public void deleteSpecific(packetInfo p){
//
//        Log.v(TAG,"DELETE REQUEST FROM PORT " + p.queryingNode + " FOR KEY:: " + p.contentKey + "");
//        if(hm.containsKey(p.contentKey))
//        {
//            hm.remove(p.contentKey);
//        }
//        Log.v(TAG, "  SENDING DEL REPLY TO QUERYING NODE:: " + p.queryingNode + " FOR KEY:: " + p.contentKey);
//        packetInfo pDel = new packetInfo("DeleteSpecific",p.contentKey, p.queryingNode, p.queryingNode);
//        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pDel);
//    }
//
//
//    @Override
//    public String getType(Uri uri) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public synchronized Uri insert(Uri uri, ContentValues values) {
//        // TODO Auto-generated method stub
//
//        String key = values.get("key").toString();             // can also use values.getAsString();
//        String value = values.get("value").toString();
//        packetInfo pInsert = new packetInfo(key, value, false, portStr);
//        Log.v(TAG, " Nodes: ");
//        ni.displayNodes();
//
//        Log.v(TAG,"Insert: Key: " + key + " Value: "+ value);
//        insertValues(pInsert);
//        try{
//            Thread.sleep(1000);
//        }catch(Exception e){
//            Log.e("Insert Error: ", "is: "+ e);
//        }
//        return uri;
//    }
//
//    public void insertValues(packetInfo pInsert) {
//        if(recoveredInsert == 1)
//        {
//            try {
//                Log.v("REPLICATE FLAG VALUE: ", pInsert.replicateFlag + " ");
//                if (pInsert.replicateFlag == false) {
//                    getPartition("Insert", pInsert.contentKey, pInsert.contentValue, pInsert.portId);
//                } else {
//                    Log.v("REPLICATE KEY:", pInsert.contentKey + " in  port " + portStr);
//                    if (pInsert.msgType.equals("CoordinatorInsert")) {
//                        hm.put(pInsert.contentKey, pInsert.contentValue);
//                        hmCoord.put(pInsert.contentKey, pInsert.contentValue);
//                    } else if (pInsert.msgType.equals("Replicate")) {
//                        hm.put(pInsert.contentKey, pInsert.contentValue);
//                        hmReplica.put(pInsert.contentKey, pInsert.coordPort);
//                        Log.v(TAG, "COORD PORT FOR KEY::" + pInsert.contentKey + " IS " + pInsert.coordPort);
//                    }
//
//                }
//            } catch (Exception e) {
//            }
//        }
//        else{
//            while(recoveredInsert!=2){
//                ;
//            }
//            recoveredInsert = 1;
//            insertValues(pInsert);
//        }
//    }
//
//    public void getPartition(String queryType, String key, String value, String port){
//        try{
//            String hashKey = genHash(key);
//            NodeInsertion n = ni.findNode(port);
//            NodeInsertion nodeCurr = (NodeInsertion) n.clone();
//            NodeInsertion nodeNext = nodeCurr.getNext(nodeCurr);
//            NodeInsertion nodePrev = nodeCurr.getPrev(nodeCurr);
//
//            int size =0;
//            while(size<5){
//                String hashKeyCurrent = nodeCurr.getHashNode(nodeCurr);
//                String hashKeyNext = nodeNext.getHashNode(nodeNext);
//                String hashKeyPrev = nodePrev.getHashNode(nodePrev);
//
//                if (   (hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
//                        ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
//                                ( hashKey.compareTo(hashKeyCurrent) <= 0))      ||   (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 ) )
//                {
//                    if(queryType.equals("Insert")){
//                        Log.v(TAG,"COORDINATOR FOUND: " + nodeCurr.getNode(nodeCurr));
//
//                        String coord= new String(nodeCurr.getNode(nodeCurr));
//                        NodeInsertion replicateNode1 = nodeCurr.getNext(nodeCurr);
//                        NodeInsertion replicateNode2 = replicateNode1.getNext(replicateNode1);
//                        String replicatePort1 =  new String(replicateNode1.getNode(replicateNode1));
//                        String replicatePort2 = new String(replicateNode2.getNode(replicateNode2));
//
//                        Log.v("PARTITION NODE:", key + " PORT: "+ coord );
//                        Log.v("SEND REPLICATE1:", key + " FROM PORT: " + portStr + " TO PORT: " + replicatePort1 );
//                        Log.v("SEND REPLICATE2:", key + " FROM PORT: " + portStr + " TO PORT: " + replicatePort2 );
//
//                        packetInfo pCoord = new packetInfo("CoordinatorInsert", key, value,true, coord, coord);
//                        packetInfo pReplicate1 = new packetInfo("Replicate",key, value, true, replicatePort1,coord);
//                        packetInfo pReplicate2 = new packetInfo("Replicate", key, value, true, replicatePort2,coord);
//
//                        for(int i = 1; i<=3; i++){
//                            if(i==1)
//                                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pCoord);
//                            if(i==2)
//                                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pReplicate1);
//                            if(i==3)
//                                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pReplicate2);
//                        }
//                        break;
//                    }
//                    if(queryType.equals("Query")){
//                        Log.v(TAG, "IF-- PARTITION PORT FOR SPECIFIC QUERY: "+ nodeCurr.getNode(nodeCurr) );
//
//                        NodeInsertion replicateNode1 = nodeCurr.getNext(nodeCurr);
//                        NodeInsertion replicateNode2 = replicateNode1.getNext(replicateNode1);
//
//                        String p = new String(nodeCurr.getNode(nodeCurr));
//                        String replicatePort1 =  new String(replicateNode1.getNode(replicateNode1));
//                        String replicatePort2 = new String(replicateNode2.getNode(replicateNode2));
//
//                        if(nodeCurr.getNode(nodeCurr).equals(portStr)){
//                            specificHm.put(key,hm.get(key));
//                        }
//                        else{
//                            packetInfo pQuery = new packetInfo("QuerySpecific", key, specificHm, portStr, p);
//                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery);
///*
//                            packetInfo pQuery1 = new packetInfo("QuerySpecific", key, specificHm, portStr, replicatePort1);
//                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery1);
//
//                            packetInfo pQuery2 = new packetInfo("QuerySpecific", key, specificHm, portStr, replicatePort2);
//                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery2);*/
//                            //Thread.sleep(1000);
//                            specQueryWait();
//                        }
//                        while(specificHm.get(key)==null){
//                            Log.v(TAG,"?????? QUERY NOT FOUND!!! " + key );
//                            try{
//                                Thread.sleep(100);
//                            }catch(Exception e){
//                                Log.e("Insert Error: ", "is: "+ e);
//                            }
//                        }
//                        return;
//                    }
//                    if(queryType.equals("DeleteSpecific")){
//                        Log.v(TAG, "PARTITION PORT FOR SPECIFIC DELETE: "+ nodeCurr.getNode(nodeCurr) );
//                        NodeInsertion replicateNode1 = nodeCurr.getNext(nodeCurr);
//                        NodeInsertion replicateNode2 = replicateNode1.getNext(replicateNode1);
//
//                        String coord = new String(nodeCurr.getNode(nodeCurr));
//                        String replicatePort1 =  new String(replicateNode1.getNode(replicateNode1));
//                        String replicatePort2 = new String(replicateNode2.getNode(replicateNode2));
//
//                        packetInfo pQuery = new packetInfo("DeleteSpecific",key, portStr, coord);
//                        packetInfo pDel1 = new packetInfo("DeleteSpecific",key, portStr, replicatePort1);
//                        packetInfo pDel2 = new packetInfo("DeleteSpecific",key, portStr, replicatePort2);
//
//
//                        Log.v(TAG," DEL REQUEST SENT TO COORD " + coord +" FOR KEY: " + key);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery);
//                        //deleteWait();
//
//                        Log.v(TAG," DEL REQUEST SENT TO REP1 " + replicatePort1 +" FOR KEY: " + key);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pDel1);
//                        //deleteWait();
//
//                        Log.v(TAG," DEL REQUEST SENT TO REP2 " + replicatePort2 + " FOR KEY: " + key);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pDel2);
//                        //deleteWait();
//
//                        Thread.sleep(1000);
//
//                        Log.v(TAG," AFTER DELETING!!! ");
//                        break;
//                    }
//                    if(queryType.equals("Recover")){
//                        NodeInsertion node = ni.findNode(port);
//                        NodeInsertion replicateNode1 = node.getPrev(node);
//                        NodeInsertion replicateNode2 = replicateNode1.getPrev(replicateNode1);
//
//                        String partition = new String(nodeCurr.getNode(nodeCurr));
//                        String replicatePort1 =  new String(replicateNode1.getNode(replicateNode1));
//                        String replicatePort2 = new String(replicateNode2.getNode(replicateNode2));
//
//                        if(partition.equals(replicatePort1) || partition.equals(replicatePort2) || partition.equals(port))
//                            hm.put(key,value);
//                        break;
//                    }
//                }
//                else{
//                    size++;
//                    Log.v("FORWARDING KEY NEXT: ", key + " FROM PORT " + nodeCurr.getNode(nodeCurr) + " TO PORT " +  nodeNext.getNode(nodeNext) );
//                    nodeCurr = nodeCurr.getNext(nodeCurr);   // nodeCurr = nodeNext
//                    nodeNext = nodeCurr.getNext(nodeCurr);
//                    nodePrev = nodeCurr.getPrev(nodeCurr);
//                }
//            }
//        }
//        catch(Exception e){
//        }
//    }
//
//    @Override
//    public boolean onCreate() {
//        // TODO Auto-generated method stub
//
//        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
//        portStr = tel.getLine1Number().substring(tel.getLine1Number().length()-4);
//        myPort = String.valueOf(Integer.parseInt(portStr)*2);
//
//        Log.v("RECOVER QUERY VALUE: " , recoveredQuery + " RECOVER INSERT VALUE:" + recoveredInsert);
//        PortId_Add_Map.put(REMOTE_PORT_ID[0],REMOTE_PORT_ADD[0]);
//        PortId_Add_Map.put(REMOTE_PORT_ID[1],REMOTE_PORT_ADD[1]);
//        PortId_Add_Map.put(REMOTE_PORT_ID[2],REMOTE_PORT_ADD[2]);
//        PortId_Add_Map.put(REMOTE_PORT_ID[3],REMOTE_PORT_ADD[3]);
//        PortId_Add_Map.put(REMOTE_PORT_ID[4],REMOTE_PORT_ADD[4]);
//
//        portOrder = new String[]{REMOTE_PORT_ID[4], REMOTE_PORT_ID[1], REMOTE_PORT_ID[0], REMOTE_PORT_ID[2], REMOTE_PORT_ID[3]};
//        try{
//            for(String p: PortId_Add_Map.keySet()){
//
//                String portHash = genHash(p);
//                PortId_Hash_Map.put(p, portHash);
//                ni.insert(p,portHash);
//            }
//        }
//        catch(Exception e){
//
//        }
//
//        try{
//            FileInputStream fi = getContext().openFileInput("recover");
//            InputStreamReader r = new InputStreamReader((fi));
//            BufferedReader br = new BufferedReader(r);
//            String msg= br.readLine();
//            if(msg.equals("alreadyJoined")){
//                Log.v(TAG, "NODE RECOVERED!!! ");
//                recoveredInsert = 0;
//                recoveredQuery = 0;
//            }
//        }
//        catch(Exception e){
//            FileOutputStream fo;
//            try{
//                Log.v(TAG,"Creating file");
//                fo = getContext().openFileOutput("recover", Context.MODE_PRIVATE);
//                fo.write("alreadyJoined".getBytes());
//                fo.close();
//            }
//            catch(Exception e1){
//                Log.e(TAG, "Cannot write in the file!!");
//            }
//        }
//
//        try{
//            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
//            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
//            if(recoveredInsert==0){
//                recover();
//            }
//
//        }
//        catch(Exception e){
//            Log.e("Sckt Creation Exception","Cant create a socket!");
//        }
//        return false;
//    }
//
//    public void recover() {
//        NodeInsertion fNode = ni.findNode(portStr);
//        NodeInsertion fNodeNext = fNode.getNext(fNode);
//        NodeInsertion fNodeNextToNext = fNodeNext.getNext(fNodeNext);
//        NodeInsertion fNodePrev = fNode.getPrev(fNode);
//        NodeInsertion fNodePrevToPrev = fNodePrev.getPrev(fNodePrev);
//
//        String nodeNext = fNodeNext.getNode(fNodeNext);
//        String nodeNextToNext = fNodeNextToNext.getNode(fNodeNextToNext);
//        String nodePrev = fNodePrev.getNode(fNodePrev);
//        String nodePrevToPrev = fNodePrevToPrev.getNode(fNodePrevToPrev);
//
//        try{
//            for (int i = 1; i <= 4; i++) {
//
//                if (i == 1) {
//                    Log.v(TAG, "SEND RECOVERY REQUEST FROM FAILED PORT: " + portStr + " TO NEXT PORT: " + nodeNext);
//                    packetInfo pck = new packetInfo("RecoverAll", globalHm, portStr, nodeNext);
//                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pck);
//                    //queryWait();
//                    Thread.sleep(1000);
//                }
//
//                if (i == 2) {
//                    Log.v(TAG, "SEND RECOVERY REQUEST FROM FAILED PORT: " + portStr + " TO NEXT TO NEXT PORT: " + nodeNextToNext);
//                    packetInfo pck1 = new packetInfo("RecoverAll", globalHm, portStr, nodeNextToNext);
//                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pck1);
//                    //queryWait();
//                    Thread.sleep(1000);
//                }
//
//                if (i == 3) {
//                    Log.v(TAG, "SEND RECOVERY REQUEST FROM FAILED PORT: " + portStr + " TO PREV PORT: " + nodePrev);
//                    packetInfo pck2 = new packetInfo("RecoverAll", globalHm, portStr, nodePrev);
//                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pck2);
//                    //queryWait();
//                    Thread.sleep(1000);
//                }
//
//                if (i == 4) {
//                    Log.v(TAG, "SEND RECOVERY REQUEST FROM FAILED PORT: " + portStr + " TO PREV TO PREV PORT: " + nodePrevToPrev);
//                    packetInfo pck3 = new packetInfo("RecoverAll", globalHm, portStr, nodePrevToPrev);
//                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pck3);
//                    //queryWait();
//                    Thread.sleep(1000);
//                }
//            }
//
//        }
//        catch(Exception e){
//
//        }
//
//        //hm.putAll(globalHm);
//        Log.v(TAG,"GLOBAL HM: " + globalHm);
//        for (String key: globalHm.keySet()){
//            getPartition("Recover",key,globalHm.get(key),portStr);
//        }
//
//        recoveredInsert = 2;
//        recoveredQuery = 2;
//    }
//
//    public packetInfo recoverAll(packetInfo p){
//        int i=0;
//        HashMap<String,String> h= p.globalStorage;
//        packetInfo pck;
//
//        // put all the values in a hm
//        h.putAll(hm);
//        NodeInsertion node = ni.findNode(p.portId);
//        NodeInsertion nodeNext = node.getNext(node);
//        Log.v(TAG,"SENDING RECOVERY REPLY FROM PORT: " + portStr + " TO PORT: " + p.queryingNode + "... REPLY HM:: " + h);
//        //pck = new packetInfo("QueryAll", h, p.queryingNode, nodeNext.getNode(nodeNext));
//        pck = new packetInfo("RecoverAll", h, p.queryingNode, p.queryingNode);
//        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
//        return  pck;
//    }
//
//    @Override
//    public synchronized Cursor query(Uri uri, String[] projection, String selection,
//                                     String[] selectionArgs, String sortOrder) {
//        // TODO Auto-generated method stub
//        MatrixCursor mc = new MatrixCursor(new String[]{"key","value"});
//
//
//        try{
//            //Thread.sleep(2500);
//            if(selection.compareTo("@")==0){
//                Log.v(TAG,"@ SELECTED!! VALUES---" + hm);
//                mc = new MatrixCursor(new String[]{"key","value"});
//                for(String keys: hm.keySet()){
//                    String k = keys;
//                    String v = hm.get(k);
//                    String content[]= {k,v};
//                    mc.addRow(content);
//                }
//            }
//            else if(selection.compareTo("*")==0){
//
//                NodeInsertion fNode = ni.findNode(portStr);
//                NodeInsertion fNodeNext = fNode.getNext(fNode);
//                NodeInsertion fNodeNextToNext = fNodeNext.getNext(fNodeNext);
//                NodeInsertion fNodePrev = fNode.getPrev(fNode);
//                NodeInsertion fNodePrevToPrev = fNodePrev.getPrev(fNodePrev);
//
//                String nodeNext = fNodeNext.getNode(fNodeNext);
//                String nodeNextToNext = fNodeNextToNext.getNode(fNodeNextToNext);
//                String nodePrev = fNodePrev.getNode(fNodePrev);
//                String nodePrevToPrev = fNodePrevToPrev.getNode(fNodePrevToPrev);
//
//                globalQueryHm.putAll(hm);
//                for(int i =1 ; i<= 4; i++){
//
//                    if(i==1)
//                    {Log.v(TAG,"SEND QUERY ALL REQUEST FROM PORT: " + portStr + " TO NEXT PORT: " + nodeNext);
//                        packetInfo pck = new packetInfo("QueryAll", globalQueryHm, portStr, nodeNext);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
//                        //queryAllWait();
//                        Thread.sleep(1000);
//                    }
//
//                    if(i==2)
//                    {Log.v(TAG,"SEND QUERY ALL REQUEST FROM PORT: " + portStr + " TO NEXT TO NEXT PORT: " + nodeNextToNext);
//                        packetInfo pck1 = new packetInfo("QueryAll", globalQueryHm, portStr, nodeNextToNext);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck1);
//                        //queryAllWait();
//                        Thread.sleep(1000);}
//
//                    if(i==3)
//                    {Log.v(TAG,"SEND QUERY ALL REQUEST FROM PORT: " + portStr + " TO PREV PORT: " + nodePrev);
//                        packetInfo pck2 = new packetInfo("QueryAll", globalQueryHm, portStr, nodePrev);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck2);
//                        //queryAllWait();
//                        Thread.sleep(1000);}
//
//                    if(i==4)
//                    {Log.v(TAG,"SEND QUERY ALL REQUEST FROM PORT: " + portStr + " TO PREV TO PREV PORT: " + nodePrevToPrev);
//                        packetInfo pck3 = new packetInfo("QueryAll", globalQueryHm, portStr, nodePrevToPrev);
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck3);
//                        //queryAllWait();
//                        Thread.sleep(1000);}
//                }
//
//
//
//                for(String key: globalQueryHm.keySet()){
//                    String content[] = {key, globalQueryHm.get(key)};
//                    mc.addRow(content);
//                }
//                if (mc.moveToFirst()) {
//                    do {
//                        int keyIndex = mc.getColumnIndex(KEY_FIELD);
//                        int valueIndex = mc.getColumnIndex(VALUE_FIELD);
//                        String returnKey = mc.getString(keyIndex);
//                        String returnValue = mc.getString(valueIndex);
//                        Log.v("Cursor Key: "+returnKey,"Cursor Value: "+ returnValue);
//
//                    } while (mc.moveToNext());
//                }
//                Log.v(TAG,"* SELECTED!! VALUES---" + globalQueryHm);
//            }
//            else{
//                getPartition("Query",selection, "", portStr);
//                Log.v(TAG,"AFTER WAIT: QUERY KEY: "+ selection + " AND VALUE: " + specificHm.get(selection));
//                String content[] = {selection, specificHm.get(selection)};
//                mc.addRow(content);
//                specificHm.remove(selection);
//            }
//        }
//        catch(Exception e){
//
//        }
//        Log.v(TAG,"BEFORE RETURNING MC FOR KEY: " + selection);
//        if (mc.moveToFirst()) {
//            do {
//                int keyIndex = mc.getColumnIndex(KEY_FIELD);
//                int valueIndex = mc.getColumnIndex(VALUE_FIELD);
//
//                String returnKey = mc.getString(keyIndex);
//                String returnValue = mc.getString(valueIndex);
//                Log.v("Cursor Key:----- "+returnKey,"Cursor Value:------ "+ returnValue+ " check infinity:" + selection);
//
//            } while (mc.moveToNext());
//        }
//        return mc;
//    }
//    public packetInfo queryAll(packetInfo p){
////        int i=0;
////        HashMap<String,String> h= p.globalStorage;
////        packetInfo pck;
////
////        // put all the values in a hm
////        h.putAll(hm);
////        try{
////            Thread.sleep(2500);
////        }
////        catch(Exception e){}
////
////        NodeInsertion node = ni.findNode(p.portId);
////        NodeInsertion nodeNext = node.getNext(node);
////        pck = new packetInfo("QueryAll", h, p.queryingNode, nodeNext.getNode(nodeNext));
////        Log.v(TAG,"SENDING QUERY ALL REPLY TO PORT:::"+ nodeNext.getNode(nodeNext) + " QUERYING NODE: "+ p.queryingNode + " REPLY IS:: " +h);
////        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
////        return  pck;
//
//
//        int i=0;
//        HashMap<String,String> h= p.globalStorage;
//        packetInfo pck;
//
//        // put all the values in a hm
//        h.putAll(hm);
//        NodeInsertion node = ni.findNode(p.portId);
//        NodeInsertion nodeNext = node.getNext(node);
//        //pck = new packetInfo("QueryAll", h, p.queryingNode, nodeNext.getNode(nodeNext));
//        pck = new packetInfo("QueryAll", h, p.queryingNode, p.queryingNode);
//        Log.v(TAG,"SENDING QUERY ALL REPLY TO PORT:::"+ nodeNext.getNode(nodeNext) + " QUERYING NODE: "+ p.queryingNode + " REPLY IS:: " +h);
//        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
//        return  pck;
//    }
//    public packetInfo querySpecific(packetInfo p){
//
//        //check whether Shared Pref contains the key or not
//        if(hm.containsKey(p.contentKey)){
//            Log.v(TAG, "QUERY SPECIFIC: FOUND KEY: " + p.contentKey + " VALUE: " + hm.get(p.contentKey) + " AT PORT: " + portStr + " OR " + p.portId );
//            p.globalStorage.put(p.contentKey,hm.get(p.contentKey));
//            Log.v(TAG, "QUERY SPECIFIC: HASH MAP: " + p.contentKey + p.globalStorage);
//            packetInfo pQuerySpecificRep = new packetInfo("QuerySpecific", p.contentKey, hm.get(p.contentKey), p.globalStorage, p.queryingNode, p.queryingNode); // Sending The specific query result to the querying node
//            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pQuerySpecificRep);
//        }
//        return p;
//    }
//
//    public void queryWait(){
//        waitStatus = true;
//        while(waitStatus);
//    }
//    public void deleteWait(){
//        delWait = true;
//        while(delWait);
//    }
//    public void queryAllWait(){
//        queryAllWait = true;
//        while(queryAllWait);
//    }
//
//    public void specQueryWait(){
//        specQueryWait = true;
//        while(specQueryWait);
//    }
//
//    @Override
//    public int update(Uri uri, ContentValues values, String selection,
//                      String[] selectionArgs) {
//        // TODO Auto-generated method stub
//        return 0;
//    }
//
//    private String genHash(String input) throws NoSuchAlgorithmException {
//        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
//        byte[] sha1Hash = sha1.digest(input.getBytes());
//        Formatter formatter = new Formatter();
//        for (byte b : sha1Hash) {
//            formatter.format("%02x", b);
//        }
//        return formatter.toString();
//    }
//
//    private class ServerTask extends AsyncTask<ServerSocket, String, Void>{
//        private Uri buildUri(String scheme, String authority) {
//            Uri.Builder uriBuilder = new Uri.Builder();
//            uriBuilder.authority(authority);
//            uriBuilder.scheme(scheme);
//            return uriBuilder.build();
//        }
//
//        @Override
//        protected Void doInBackground(ServerSocket ... sockets) {
//            ServerSocket serverSocket = sockets[0];
//            Socket socket = null;
//            Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
//
//            do{
//                try{
//                    socket = serverSocket.accept();
//                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
//                    packetInfo p = (packetInfo) is.readObject();
//                    if(p.msgType.equals("Replicate") || p.msgType.equals("CoordinatorInsert")){
//                        insertValues(p);
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//                    else if( p.msgType.equals("QueryAll")){
//
//                        if(p.portId.equals(p.queryingNode)){
//                            Log.v(TAG,"GOT ALL QUERIES:  ");
//                            globalQueryHm.putAll(p.globalStorage);
//                            //globalQueryHm.putAll(hm);
//                            Thread.sleep(2000);
//                            queryAllWait = false;
//                        }
//                        else{
//                            queryAll(p);
//                        }
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//
//                    else if (p.msgType.equals("RecoverAll")){
//
//                        if(p.portId.equals(p.queryingNode)){
//                            Log.v(TAG," RECEIVED RECOVERY REPLY!!");
//                            globalHm.putAll(p.globalStorage);
//                            waitStatus = false;
//                        }
//                        else{
//                            recoverAll(p);
//                        }
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//                    else if(p.msgType.equals("QuerySpecific")){
//                        Log.v("SERVER QUERY SPEC: ", "QUERYING NODE:: "+ p.queryingNode + " TO PORT:: " + p.portId + " FOR KEY: " + p.contentKey);
//
//                        if(p.portId.equals(p.queryingNode)){
//                            if(p.contentValue!=null)
//                            {
//                                Log.v("IF SERVER QRY SPECIFIC:", p.contentKey + " VALUE: " + p.contentValue + " QUERYING NODE: " + p.queryingNode + " CURRENT PORT: " + p.portId + "HASH MAP: " + p.globalStorage );
//                                specificHm.put(p.contentKey,p.contentValue);
//                            }
//                            else
//                            {
//                                Log.v("IF SERVER QRY SPECIFIC:", p.contentKey + " VALUE: " + hm.get(p.contentKey) + " QUERYING NODE: " + p.queryingNode + " CURRENT PORT: " + p.portId + "HASH MAP: " + p.globalStorage );
//                                specificHm.put(p.contentKey,hm.get(p.contentKey));
//                            }
//
//                            Log.v("IF SERVER HM: ", " BEFORE RELEASING LOCK: "+specificHm);
//                            specQueryWait = false;
//                        }
//                        else
//                        {
//                            Log.v("ElSE SERVER: QRY SPCFC", p.contentKey + " VALUE: " + hm.get(p.contentKey) + " QUERYING NODE: " + p.queryingNode + " CURRENT PORT: " + p.portId );
//                            querySpecific(p);
//                        }
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//
//                    else if (p.msgType.equals("DeleteAll")){
//                        if (!p.portId.equals(p.queryingNode)) {
//                            deleteAll(p);
//                        }
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//                    else if (p.msgType.equals("DeleteSpecific")){
//                        if (p.queryingNode.equals(p.portId))
//                        {   if(hm.containsKey(p.contentKey))
//                            hm.remove(p.contentKey);
//                            delWait=false;
//                            Log.v(TAG, "DELETE REPLY FOR KEY!! " + p.contentKey);
//                        }
//                        else
//                            deleteSpecific(p);
//                        ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                        os1.writeObject(p);
//                    }
//
//                }
//                catch(Exception e){
//
//                }
//            }while (true);
//        }
//    }
//
//    private class ClientTask extends AsyncTask<packetInfo, Void, Void> {
//        @Override
//        protected Void doInBackground(packetInfo...pack){
//            packetInfo p = pack[0];
//            try {
//                Log.v("CLIENT: ", p.msgType + "   KEY: "+ p.contentKey +  " TO PORT: " + p.portId + "  ");
//                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(p.portId)*2);
//                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
//                os.writeObject(p);
//                Thread.sleep(500);
//                ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
//                packetInfo p1 = (packetInfo) is.readObject();
//                //socket.setSoTimeout();
//                Log.v(TAG,"AFTER SLEEP: ");
//            }
//            catch (IOException io){
//                Log.v(TAG,"SOCKET TIME OUT" + p.portId + " MESSAGE TYPE: " + p.msgType);
//                if(p.msgType.equals("RecoverAll"))
//                {
//
//                    Log.v(TAG," RECOVER REPLY FROM FAILED NODE!! ");
//                    waitStatus = false;
//                }
//                if(p.msgType.equals("QueryAll")){
//                    {
//                        Log.v(TAG," QUERY ALL REPLY FROM FAILED NODE!! ");
//                        queryAllWait = false;
//                    }
//                    if(p.msgType.equals("DeleteSpecific"))
//                    {
//                        Log.v(TAG," DEL REPLY FROM FAILED NODE!! ");
//                        delWait = false;
//                    }
////                    NodeInsertion fNext = ni.findNode(p.portId);
////                    String next = fNext.getNext(fNext).getNode(fNext.getNext(fNext));
////                    Log.v(TAG, "AFTER FAILURE: SENDING QUERY ALL REQuEST TO: " + next + " AND QUERYING NODE IS: " + p.queryingNode);
////                    packetInfo pi = new packetInfo("QueryAll", globalHm, p.queryingNode,next);
////                    try{
////                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(next)*2);
////                        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
////                        os.writeObject(pi);
////                        //Thread.sleep(500);
////                        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
////                        packetInfo p1 = (packetInfo) is.readObject();
////                    }
////                    catch (Exception e){
////
////                    }
//                }
//                if(p.msgType.equals("QuerySpecific")){
//                    NodeInsertion fNext = ni.findNode(p.portId);
//                    String next = fNext.getNext(fNext).getNode(fNext.getNext(fNext));
//                    Log.v(TAG, "AFTER FAILURE: SENDING QUERY SPECIFIC REQUEST TO: " + next + " AND QUERYING NODE IS: " + p.queryingNode);
//                    packetInfo pi = new packetInfo("QuerySpecific", p.contentKey, p.globalStorage, p.queryingNode, next);
//                    try{
//                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(next)*2);
//                        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
//                        os.writeObject(pi);
//                        //Thread.sleep(500);
//                        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
//                        packetInfo p1 = (packetInfo) is.readObject();
//                    }
//                    catch (Exception e){
//
//                    }
//                }
//
//            }
//            catch(Exception e){
//                Log.e("Connection err: ", "is: " + e);
//            }
//            return null;
//        }
//    }
//}
