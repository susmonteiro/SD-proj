package pt.tecnico.rec.frontend;

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
    private RecordFrontend frontend;
    
    public RecordFrontendReplicationWrapper(String zooHost, int zooPort) throws ZKNamingException {        
        initFrontends(zooHost, zooPort);
    }

    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, boolean debug) throws ZKNamingException {
        DEBUG = debug;
        initFrontends(zooHost, zooPort);
    }

    public void close() {
        frontend.close();
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
        frontend = new RecordFrontend(target, DEADLINE_MS, true);
    }

    public String getPath() {
        return frontend.getPath();
    }
    /* function readReplicated()
            perform read all replicas
            wait for quorum responses
            return to client
    */


    /* Replication Logic */
    /* ================= */

    public ReadResponse readReplicated(RegisterRequest request) throws StatusRuntimeException {
        // TODO logic

        ReadResponse response;
		try{
			// Finally, make the call using the stub with timeout of 2 seconds
			response = frontend.read(request);

		} catch(StatusRuntimeException e){
			// If the timeout time has expired, stop the client
			if(Status.DEADLINE_EXCEEDED.getCode() == e.getStatus().getCode())
				debug("#readReplicated\tServer timed-out.");
            throw e;

		}

        return response;
    }

    public WriteResponse writeReplicated(RegisterRequest request) {
        // TODO logic
        WriteResponse response;

        try {
            response = frontend.write(request);
        
        } catch(StatusRuntimeException e){
			// If the timeout time has expired, stop the client
			if(Status.DEADLINE_EXCEEDED.getCode() == e.getStatus().getCode())
				debug("#writeReplicated\tServer timed-out.");
            throw e;

		}

        return response;

    }

    public PingResponse pingReplicated(PingRequest request) {
        // TODO logic

        return frontend.ping(request);
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
        
        PingResponse response = pingReplicated(request);
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
