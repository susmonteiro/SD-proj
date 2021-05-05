package pt.tecnico.rec.frontend;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.concurrent.TimeUnit;


public class RecordFrontend implements AutoCloseable {
	private boolean DEBUG = false;

	private final ManagedChannel channel;
	private final RecordServiceGrpc.RecordServiceStub asyncStub;
	private final RecordServiceGrpc.RecordServiceBlockingStub blockingStub;
	private ZKRecord zkRecord;

    private final int timeout;
    
    // Constructors when using zookeeper
    public RecordFrontend(ZKRecord zkRecord, int timeoutMS) {

        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        this.channel = ManagedChannelBuilder.forTarget(zkRecord.getURI()).usePlaintext().build();
        
        // Create a blocking stub.
        asyncStub = RecordServiceGrpc.newStub(channel);
		blockingStub = RecordServiceGrpc.newBlockingStub(channel);
        
        this.zkRecord = zkRecord;
        this.timeout = timeoutMS;
    }
    
    public RecordFrontend(ZKRecord zkRecord, int timeoutMS, boolean debug) {
        this(zkRecord, timeoutMS);
		this.DEBUG = debug;
	}
    
    // Constructors when receiving rec information directly (LEGACY)
    public RecordFrontend(String host, int port, int timeoutMS) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a non blocking stub.
		asyncStub = RecordServiceGrpc.newStub(channel);
		blockingStub = RecordServiceGrpc.newBlockingStub(channel);

        this.timeout = timeoutMS;
    }

	public RecordFrontend(String host, int port, int timeoutMS, boolean debug) {
		this(host, port, timeoutMS);
		this.DEBUG = debug;
	}
	
	public String getPath() {
		return zkRecord.getPath();
	}

	public String getURI() {
		return zkRecord.getURI();
	}

	public void read(RegisterRequest request, ResponseObserver<ReadResponse> collector) {
		asyncStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).read(request, collector);
	}

	public void write(RegisterRequest request, ResponseObserver<WriteResponse> collector) {
		asyncStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).write(request, collector);
	}

	public PingResponse ping(PingRequest request) {
		return blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).ping(request);
	}

    @Override
	public final void close() {
		channel.shutdown();
	}

	/** Helper method to print debug messages. */
	private void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@RecordFrontend\t" +  debugMessage);
	}
}