package pt.tecnico.rec.frontend;

import java.util.List;
import java.util.ArrayList;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.Status;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.StatusRuntimeException;

public class RecordFrontendReplicationWrapper extends MessageHelper {
    private boolean DEBUG = false;
    public static final String ZOO_DIR = "/grpc/bicloin/rec";
    public static final int DEADLINE_MS = 2000;

    private ZKNaming zkNaming;
    private List<RecordFrontend> replicas = new ArrayList<RecordFrontend>();
    private int clientID;
    private int quorum;
    
    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, int cid) throws ZKNamingException {        
        this.clientID = cid;
        initReplicas(zooHost, zooPort);
    }

    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, int cid, boolean debug) throws ZKNamingException {
        DEBUG = debug;
        this.clientID = cid;
        initReplicas(zooHost, zooPort);
    }

    public void close() {
        for (RecordFrontend frontend : replicas)
            frontend.close();
    }
    
    private void initReplicas(String zooHost, int zooPort) throws ZKNamingException {
        // find all replicas
        // for todas as replicas
            // criar frontend para essa replica
            // guardar em lista de frontends
            
        debug("#initReplicas\tContacting ZooKeeper at " + zooHost + ":" + zooPort);
        zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));

        // list lookup
        ArrayList<ZKRecord> records = new ArrayList<>(zkNaming.listRecords(ZOO_DIR));
        debug("#initReplicas\tZk records: " + records);
        String target = records.get(0).getURI();
        debug("#initReplicas\tZK 0 target: " + target);
        replicas.add(new RecordFrontend(target, DEADLINE_MS, true));

        quorum = replicas.size()/2 + 1;
        debug("#initReplicas\tQuorum size: " + quorum);
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
        ResponseObserver<WriteResponse> collector = new ResponseObserver<WriteResponse>(this.quorum, this.replicas.size(), DEBUG);

        synchronized(collector) {
            do {
                try {
                    for (RecordFrontend frontend : replicas)
                        frontend.write(request, collector);
                    
                    collector.wait();
                
                } catch(InterruptedException e) {
                    throw Status.ABORTED.withDescription("Read call was interrupted. Operation might not be totally processed (UKNOWN state)").asRuntimeException();
                
                } finally {
                    if (collector.isReplicaDown()) {
                        // TODO rebuild frontend replicas
                    }
                    if (collector.getLogicException() != null) { debug("throwing: " + collector.getLogicException()); throw collector.getLogicException(); }
                }

            } while(!collector.isQuorumMet());

        }
    }

    public List<PingResponse> pingReplicated(PingRequest request) throws StatusRuntimeException {
        // TODO logic
        // TODO
        ResponseObserver<PingResponse> collector = new ResponseObserver<PingResponse>(this.replicas.size(), this.replicas.size(), DEBUG);
        List<PingResponse> responses;
        synchronized(collector) {
            
            do {
                try {
                    replicas.get(0).ping(request, collector);

                    collector.wait();
                
                } catch(InterruptedException e) {
                    throw Status.ABORTED.withDescription("Read call was interrupted. Operation might not be totally processed (UKNOWN state)").asRuntimeException();
                
                } finally {
                    if (collector.isReplicaDown()) {
                        // TODO rebuild frontend replicas
                    }
                    if (collector.getLogicException() != null) { throw collector.getLogicException(); }
                }

            } while(!collector.isQuorumMet());

            responses = collector.getResponses();
        }
        
        return responses;
    }

    private ResponseObserver<ReadResponse> readReplicatedResponseObserver(RegisterRequest request) throws StatusRuntimeException {
        ResponseObserver<ReadResponse> collector = new ResponseObserver<ReadResponse>(this.quorum, this.replicas.size(), DEBUG);

        synchronized(collector) {
            do {
                try {
                    for (RecordFrontend frontend : replicas)
                        frontend.read(request, collector);
                    
                    collector.wait();
                
                } catch(InterruptedException e) {
                    throw Status.ABORTED.withDescription("Read call was interrupted. Operation might not be totally processed (UKNOWN state)").asRuntimeException();
                
                } finally {
                    if (collector.isReplicaDown()) {
                        // TODO rebuild frontend replicas
                    }
                    if (collector.getLogicException() != null) { throw collector.getLogicException(); }
                }

            } while(!collector.isQuorumMet());

        }

        return collector;
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


	public String getPing(String input) throws StatusRuntimeException {
        /* Use only with trusted id */
        PingRequest request = getPingRequest(input);
        debug("#getPing\n**Request:\n" + request);
        
        List<PingResponse> responses = pingReplicated(request);
        debug("#getPing\n**Response:\n" + responses);
        
        String output = responses.get(0).getOutput();       // TODO change to send all, done this way to compile and not have to change Logic for now
        debug("#getPing\n**Value:\n" + output);

        return output;
    }


   /** Helper method to print debug messages. */
   private void debug(Object debugMessage) {
    if (DEBUG)
        System.err.println("@RecordFrontendReplicationWrapper\t" +  debugMessage);
    }

}
