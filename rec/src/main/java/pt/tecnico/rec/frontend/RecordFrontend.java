package pt.tecnico.rec.frontend;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;

import io.grpc.StatusRuntimeException;

public class RecordFrontend extends MessageHelper implements AutoCloseable {
	private boolean DEBUG = false;
	private final ManagedChannel channel;
	private final RecordServiceGrpc.RecordServiceBlockingStub stub;
	private final String host;
	private final int port;
	
	public RecordFrontend(String host, int port) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = RecordServiceGrpc.newBlockingStub(channel);

		this.host = host;
		this.port = port;
	}

	public RecordFrontend(String host, int port, boolean debug) {
		this(host, port);
		this.DEBUG = debug;
	}
	
	public String getPath() {
		return host + ":" + port;
	}

	public ReadResponse read(RegisterRequest request) {
		return stub.read(request);
	}

	public WriteResponse write(RegisterRequest request) {
		return stub.write(request);
	}

	public PingResponse ping(PingRequest request) {
		return stub.ping(request);
	}


    @Override
	public final void close() {
		channel.shutdown();
	}

	/* Record Getters and Setters */
    /* ========================== */

    public int getBalance(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue());
        debug("#getBalance\n**Request:\n" + request);
        
        ReadResponse response = read(request);
        debug("#getBalance\n**Response:\n" + response);
        
        int value = getBalanceValue(response.getData());
        debug("#getBalance\n**Value:\n" + value);

        return value;
    }

    public void setBalance(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue(value));
        debug("#setBalance\n**Request:\n" + request);

        write(request);
    }
    

	public boolean getOnBike(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue());
        debug("#getOnBike\n**Request:\n" + request);
        
        ReadResponse response = read(request);
        debug("#getOnBike\n**Response:\n" + response);
        
        boolean value = getOnBikeValue(response.getData());
        debug("#getOnBike\n**Value:\n" + value);

        return value;
    }

    public void setOnBike(String id, boolean value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue(value));
        debug("#setOnBike\n**Request:\n" + request);
        
        write(request);
    }


    public int getNBikes(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue());
        debug("#getNBikes\n**Request:\n" + request);
        
        ReadResponse response = read(request);
        debug("#getNBikes\n**Response:\n" + response);
        
        int value = getNBikesValue(response.getData());
        debug("#getNBikes\n**Value:\n" + value);

        return value;
    }

    public void setNBikes(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue(value));
        debug("#setNBikes\n**Request:\n" + request);
        
        write(request);
    }


	public int getNPickUps(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue());
        debug("#getNPickUps\n**Request:\n" + request);
        
        ReadResponse response = read(request);
        debug("#getNPickUps\n**Response:\n" + response);
        
        int value = getNPickUpsValue(response.getData());
        debug("#getNPickUps\n**Value:\n" + value);

        return value;
    }

    public void setNPickUps(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNPickUpsAsRegisterValue(value));
        debug("#setNPickUps\n**Request:\n" + request);
        
        write(request);
    }

	public int getNDeliveries(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNDeliveriesAsRegisterValue());
        debug("#getNDeliveries\n**Request:\n" + request);
        
        ReadResponse response = read(request);
        debug("#getNDeliveries\n**Response:\n" + response);
        
        int value = getNDeliveriesValue(response.getData());
        debug("#getNDeliveries\n**Value:\n" + value);

        return value;
    }

    public void setNDeliveries(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        RegisterRequest request = getRegisterRequest(id, getRegisterNDeliveriesAsRegisterValue(value));
        debug("#setNBikes\n**Request:\n" + request);
        
        write(request);
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
			System.err.println("@RecordFrontend\t" +  debugMessage);
	}
}