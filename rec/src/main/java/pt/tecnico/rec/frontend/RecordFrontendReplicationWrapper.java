package pt.tecnico.rec.frontend;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.Status;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.StatusRuntimeException;

public class RecordFrontendReplicationWrapper extends MessageHelper {
    private static final int DELAY = 10000; // 10 seconds
    private boolean DEBUG = false;
    public static final String ZOO_DIR = "/grpc/bicloin/rec";
    public static final int DEADLINE_MS = 2000;

    private ZKNaming zkNaming;
    private List<RecordFrontend> replicas;
    private int clientID;
    private int quorum;
    
    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, int cid) {        
        this.clientID = cid;
        debug("#RecordFrontendReplicationWrapper\tContacting ZooKeeper at " + zooHost + ":" + zooPort);
        zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));
        initReplicas();
    }

    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, int cid, boolean debug) {
        this.DEBUG = debug;
        this.clientID = cid;
        debug("#RecordFrontendReplicationWrapper\tContacting ZooKeeper at " + zooHost + ":" + zooPort);
        zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));
        initReplicas();
    }

    public void close() {
        replicas.forEach((replica) -> replica.close()); 
    }
    
    private void initReplicas() {
        // list lookup
        try {
            List<ZKRecord> records = new ArrayList<>(zkNaming.listRecords(ZOO_DIR));

            // if no replicas were found, try again
            if (records.isEmpty()) { records = retryGetReplicas(); }
            
            debug("#initReplicas\tZk records: " + records);
            replicas = new ArrayList<RecordFrontend>();
            records.forEach((r) -> replicas.add(new RecordFrontend(r, DEADLINE_MS, this.DEBUG)));
                       
            quorum = replicas.size()/2 + 1;
            debug("#initReplicas\tQuorum size: " + quorum);

        } catch (ZKNamingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private List<ZKRecord> retryGetReplicas() throws ZKNamingException {
        List<ZKRecord> records;
        do {
			System.out.println("No Recs found. Retrying in 10 secs...");
			try { Thread.sleep(DELAY); }
			catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            records = new ArrayList<>(zkNaming.listRecords(ZOO_DIR));
        } while (records.isEmpty());
        return records;
    }

    public List<RecordFrontend> getReplicas() {
        return replicas;
    }

    /* Replication Logic */
    /* ================= */

    public ReadResponse readReplicated(RegisterRequest request) throws StatusRuntimeException {
        ResponseObserver<ReadResponse> collector = readReplicatedResponseObserver(request);
        return getResponse(collector);
    }
    
    public void writeReplicated(RegisterRequest request) {
        
        while(true) {
            ResponseObserver<WriteResponse> collector = new ResponseObserver<WriteResponse>(this.quorum, this.replicas.size(), this.DEBUG);
            synchronized(collector) {
                try {
                    replicas.forEach((replica) -> replica.write(request, collector));

                    collector.wait();
                
                } catch(InterruptedException e) {
                    throw Status.ABORTED.withDescription("Write call was interrupted. Operation might not be totally processed (UKNOWN state)").asRuntimeException();
                
                } finally {
                    if (collector.isReplicaDown()) { initReplicas(); /* rebuild frontend replicas */ }
                    if (collector.getLogicException() != null) { throw collector.getLogicException(); }
                }

                if (collector.isQuorumMet()) return;
            }
        } 
    }

    private ResponseObserver<ReadResponse> readReplicatedResponseObserver(RegisterRequest request) throws StatusRuntimeException {

        while(true) {
            ResponseObserver<ReadResponse> collector = new ResponseObserver<ReadResponse>(this.quorum, this.replicas.size(), this.DEBUG);
            synchronized(collector) {
                try {
                    replicas.forEach((replica) -> replica.read(request, collector));

                    collector.wait();
                
                } catch(InterruptedException e) {
                    throw Status.ABORTED.withDescription("Read call was interrupted. Operation might not be totally processed (UKNOWN state)").asRuntimeException();
                
                } finally {
                    if (collector.isReplicaDown()) { initReplicas(); /* rebuild frontend replicas */ }
                    if (collector.getLogicException() != null) { throw collector.getLogicException(); }
                }

                if (collector.isQuorumMet()) return collector;
            }
        } 
    }

    
        /* Tag */
        /* +++ */

    private RegisterRequest addTagToRegister(RegisterRequest request) {
        ResponseObserver<ReadResponse> collector = readReplicatedResponseObserver(request);
        int maxSeqNumber = getMostRecentSeqNumber(collector);
        
        RegisterTag newTag = getRegisterTag(
            maxSeqNumber + 1,
            this.clientID
        );
        return setTagToRegisterRequest(request, newTag);
    }

    private boolean isTagNewer(RegisterTag oldTag, RegisterTag newTag) {
        if (oldTag.getSeqNumber() < newTag.getSeqNumber()) { return true; }
        // useless for now, but will need this in case of multiple hubs
        if (oldTag.getSeqNumber() == newTag.getSeqNumber() && oldTag.getClientID() < newTag.getClientID()) { return true; }
        // else new tag is older than the previous
        return false;
    }

    /** Returns most recent response */
    public ReadResponse getResponse(ResponseObserver<ReadResponse> collector) {
        RegisterTag tag;
        RegisterTag latestTag;
        ReadResponse latestResponse;
        
        synchronized(collector) {
            List<ReadResponse> responses = collector.getResponses();
            
            latestTag = responses.get(0).getData().getTag();
            latestResponse = responses.get(0);

            for (ReadResponse response : responses) {
                tag = response.getData().getTag();
                if (isTagNewer(latestTag, tag)) {
                    latestTag = tag;
                    latestResponse = response;
                }
            }
        }
        
        return latestResponse;
    }

    /** returns most recent sequence number */
    public int getMostRecentSeqNumber(ResponseObserver<ReadResponse> collector) {
        int seqNumber;
        int latestSeqNumber;

        synchronized(collector) {
            List<ReadResponse> responses = collector.getResponses();

            latestSeqNumber = responses.get(0).getData().getTag().getSeqNumber();

            for (ReadResponse response : responses) {
                seqNumber = response.getData().getTag().getSeqNumber();
                if (seqNumber > latestSeqNumber) latestSeqNumber = seqNumber;
            }
        }

        return latestSeqNumber;
    }

    /* Record Getters and Setters */
    /* ========================== */

    public int getBalance(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue());
        debug("#getBalance\n**Request:\n" + request);
        
        ReadResponse response = readReplicated(request);
        debug("#getBalance\n**Response:\n" + response);
        
        int value = getBalanceValue(response.getData());
        debug("#getBalance\n**Value:\n" + value);

        return value;
    }

    public void setBalance(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */        
        RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue(value));
        request = addTagToRegister(request);
        debug("#setBalance\n**Request:\n" + request);

        writeReplicated(request);
    }

    public void setBalance(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        setBalance(id, getBalanceDefaultValue());
    }
    

	public boolean getOnBike(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue());
        debug("#getOnBike\n**Request:\n" + request);
        
        ReadResponse response = readReplicated(request);
        debug("#getOnBike\n**Response:\n" + response);
        
        boolean value = getOnBikeValue(response.getData());
        debug("#getOnBike\n**Value:\n" + value);

        return value;
    }

    public void setOnBike(String id, boolean value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue(value));
        request = addTagToRegister(request);
        debug("#setOnBike\n**Request:\n" + request);
        
        writeReplicated(request);
    }

    public void setOnBike(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        setOnBike(id, getOnBikeDefaultValue());
    }


    public int getNBikes(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue());
        debug("#getNBikes\n**Request:\n" + request);
        
        ReadResponse response = readReplicated(request);
        debug("#getNBikes\n**Response:\n" + response);
        
        int value = getNBikesValue(response.getData());
        debug("#getNBikes\n**Value:\n" + value);

        return value;
    }

    public void setNBikes(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue(value));
        request = addTagToRegister(request);
        debug("#setNBikes\n**Request:\n" + request);
        
        writeReplicated(request);
    }


	public int getNPickUps(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNPickUpsAsRegisterValue());
        debug("#getNPickUps\n**Request:\n" + request);
        
        ReadResponse response = readReplicated(request);
        debug("#getNPickUps\n**Response:\n" + response);
        
        int value = getNPickUpsValue(response.getData());
        debug("#getNPickUps\n**Value:\n" + value);

        return value;
    }

    public void setNPickUps(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNPickUpsAsRegisterValue(value));
        request = addTagToRegister(request);
        debug("#setNPickUps\n**Request:\n" + request);
        
        writeReplicated(request);
    }
    
    public void setNPickUps(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        setNPickUps(id, getNPickUpsDefaultValue());
    }

	public int getNDeliveries(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNDeliveriesAsRegisterValue());
        debug("#getNDeliveries\n**Request:\n" + request);
        
        ReadResponse response = readReplicated(request);
        debug("#getNDeliveries\n**Response:\n" + response);
        
        int value = getNDeliveriesValue(response.getData());
        debug("#getNDeliveries\n**Value:\n" + value);

        return value;
    }

    public void setNDeliveries(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNDeliveriesAsRegisterValue(value));
        request = addTagToRegister(request);
        debug("#setNBikes\n**Request:\n" + request);
        
        writeReplicated(request);
    }

    public void setNDeliveries(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        setNDeliveries(id, getNDeliveriesDefaultValue());
    }

    // ping is still a synchronous function
    // no need to meet a quorum of responses (we want a specific replica to reply)
	public String getPing(String input, RecordFrontend frontend) throws StatusRuntimeException {
        PingRequest request = getPingRequest(input);
        debug("#getPing\n**Request:\n" + request);
        
        PingResponse response = frontend.ping(request);
        debug("#getPing\n**Response:\n" + response);
        
        String output = response.getOutput();       
        debug("#getPing\n**Value:\n" + output);

        return output;
    }

    public String getPing(String input, int instance_num) throws StatusRuntimeException, ZKNamingException {
		final ZKRecord target = zkNaming.lookup(ZOO_DIR + "/" + instance_num);

        RecordFrontend frontend = new RecordFrontend(target, DEADLINE_MS, this.DEBUG);

        String response = getPing(input, frontend);

        frontend.close();
        return response;
    }



    public Map<String, Boolean> getSysStatus() {
        debug("#getSysStatus");
        // Forcing discover of new Record Replicas
        initReplicas();

        Map<String, Boolean> responses = new HashMap<String, Boolean>();
        for (RecordFrontend replica : replicas) {
            boolean status = false;
            try {
                this.getPing("@sysStatus", replica);
                status = true;
            } catch (StatusRuntimeException e) {
                // if StatusRuntimeException then server is down
                status = false;
            }
            debug("Path: " + replica.getPath());
            responses.put(replica.getPath(), status);
        }

        return responses;
    }

   /** Helper method to print debug messages. */
   private void debug(Object debugMessage) {
    if (this.DEBUG)
        System.err.println("@RecordFrontendReplicationWrapper\t" +  debugMessage);
    }

}
