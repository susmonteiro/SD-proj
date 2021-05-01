package pt.tecnico.rec.frontend;

import java.util.ArrayList;

import pt.tecnico.rec.grpc.Rec.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.StatusRuntimeException;

public class RecordFrontendReplicationWrapper extends MessageHelper {
    private boolean DEBUG = false;
    public static final String ZOO_DIR = "/grpc/bicloin/rec";

    private ZKNaming zkNaming;
    private RecordFrontend frontend;
    
    public RecordFrontendReplicationWrapper(String zooHost, int zooPort) throws ZKNamingException {        
        initFrontends(zooHost, zooPort);
    }

    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, boolean debug) throws ZKNamingException {
        DEBUG = debug;
        initFrontends(zooHost, zooPort);
    }
    
    private void initFrontends(String zooHost, int zooPort) throws ZKNamingException {
        // find all replicas
        // for todas as replicas
            // criar frontend para essa replica
            // guardar em lista de frontends
            
        debug("Contacting ZooKeeper at " + zooHost + ":" + zooPort);
        zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));

        // list lookup
        ArrayList<ZKRecord> records = new ArrayList<>(zkNaming.listRecords(ZOO_DIR));
        debug("Zk records: " + records);
        String target = records.get(0).getURI();
        debug("ZK 0 target: " + target);
        frontend = new RecordFrontend(target, true);
    }

    /* function readReplicated()
            perform read all replicas
            wait for quorum responses
            return to client
    */


    /* Replication Logic */
    /* ================= */

    private ReadResponse readReplicated(RegisterRequest request) {
        // TODO logic

        return frontend.read(request);
    }

    private WriteResponse writeReplicated(RegisterRequest request) {
        // TODO logic

        return frontend.write(request);
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
        debug("#setBalance\n**Request:\n" + request);

        writeReplicated(request);
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
        debug("#setOnBike\n**Request:\n" + request);
        
        writeReplicate(request);
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
        debug("#setNPickUps\n**Request:\n" + request);
        
        writeReplicated(request);
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
        debug("#setNBikes\n**Request:\n" + request);
        
        writeReplicated(request);
    }


	public String getPing(String input) throws StatusRuntimeException {
        /* Use only with trusted id */
        PingRequest request = getPingRequest(input);
        debug("#getPing\n**Request:\n" + request);
        
        PingResponse response = ping(request);
        debug("#getPing\n**Response:\n" + response);
        
        String output = response.getOutput();
        debug("#getPing\n**Value:\n" + output);

        return output;
    }


   /** Helper method to print debug messages. */
   private void debug(Object debugMessage) {
    if (DEBUG)
        System.err.println("@RecordFrontendReplicationWrapper\t" +  debugMessage);
    }

}
