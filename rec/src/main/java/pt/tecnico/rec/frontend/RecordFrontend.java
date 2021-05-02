package pt.tecnico.rec.frontend;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import java.util.concurrent.TimeUnit;


public class RecordFrontend implements AutoCloseable {
	private boolean DEBUG = false;

	private final ManagedChannel channel;
	private final RecordServiceGrpc.RecordServiceBlockingStub stub;
	private String path;

    private final int timeout;
    
    // Constructors when using zookeeper
    public RecordFrontend(String target, int timeoutMS) {
        debug("Located server at " + target);
        
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        
        // Create a blocking stub.
        stub = RecordServiceGrpc.newBlockingStub(channel);
        
        this.path = target;
        this.timeout = timeoutMS;
    }
    
    public RecordFrontend(String target, int timeoutMS, boolean debug) {
        this(target, timeoutMS);
		this.DEBUG = debug;
	}
    
    // Constructors when receiving rec information directly (LEGACY)
    public RecordFrontend(String host, int port, int timeoutMS) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = RecordServiceGrpc.newBlockingStub(channel);
        this.timeout = timeoutMS;
    }

	public RecordFrontend(String host, int port, int timeoutMS, boolean debug) {
		this(host, port);
		this.DEBUG = debug;
	}
	
	public String getPath() {
		return path;
	}

	public ReadResponse read(RegisterRequest request) {
		return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).read(request);
	}

	public WriteResponse write(RegisterRequest request) {
		return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).write(request);
	}

	public PingResponse ping(PingRequest request) {
		return stub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS).ping(request);
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