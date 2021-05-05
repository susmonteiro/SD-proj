package pt.tecnico.rec.frontend;

import java.util.List;
import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;


public class ResponseObserver<R> implements StreamObserver<R> {
    private static boolean DEBUG = false;
    private final int totalReplicas;
    private volatile int totalResponses = 0;
    private int goal;
    private List<R> responses = new ArrayList<R>();
    private volatile boolean replicaDown = false;
    private volatile StatusRuntimeException logicException = null;

    public ResponseObserver(int nResponsesGoal, int totalReplicas, boolean debug) {
        this.goal = nResponsesGoal;
        this.totalReplicas = totalReplicas;
        DEBUG = debug;
    }
    
    @Override
    public void onNext(R r) {
        debug("Received response: " + r);
        synchronized(this) {
            debug("Adding response");
            this.responses.add(r);
            this.totalResponses++;
        }
    }

    @Override
    public void onError(Throwable throwable) {
        debug("Received error: " + throwable);
        Status status = Status.fromThrowable(throwable);
        debug("Status error received: " + status);

        debug("Status code received: " + status.getCode()
            + "check if is UNAVAILABLE OR DEADLINE_EXCEEDED: " +
            (status.getCode() == Status.UNAVAILABLE.getCode() 
            || status.getCode() == Status.DEADLINE_EXCEEDED.getCode()));
        
        synchronized(this) {
            this.totalResponses++;
            // Mark connection error for handling after 
            if (status.getCode() == Status.UNAVAILABLE.getCode() 
                || status.getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                this.replicaDown = true;
                // this check is needed if the last answer is a error (which means onCompleted() is not called)
                if (totalResponses == totalReplicas) {
                    debug("Notify waiting");
                    this.notifyAll();
                }
                debug("Server connection error logged.");
            } else {
                debug("Server connection error logged.");
                logicException = status.asRuntimeException();
                this.notifyAll();
            }
        }
    }

    @Override
    public void onCompleted() {
        debug("Request completed");
        synchronized(this) {
            debug("Checking responses size");
            if (responses.size() == goal || totalResponses == totalReplicas) {
                debug("Notify waiting");
                this.notifyAll();
            }
        }
    }

    public List<R> getResponses() {
        // copy prevents synchronization issues, we only add new elements
        return new ArrayList<R>(responses);
    }

    public StatusRuntimeException getLogicException() {
        return logicException;
    }

    public boolean isReplicaDown() {
        return replicaDown;
    }

    public boolean isQuorumMet() {
        synchronized(this) {
            return responses.size() >= goal;
        }
    }
    
    /** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@ResponseObserver" + debugMessage);
	}
}