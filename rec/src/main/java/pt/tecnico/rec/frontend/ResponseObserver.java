package pt.tecnico.rec.frontend;

import java.util.List;
import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.Debug;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;


public class ResponseObserver<R> implements StreamObserver<R> {
    private static Debug DEBUG = Debug.NO_DEBUG;
    private final int totalReplicas;
    private volatile int totalResponses = 0;
    private int goal;
    private List<R> responses = new ArrayList<R>();
    private volatile boolean replicaDown = false;
    private volatile StatusRuntimeException logicException = null;

    public ResponseObserver(int nResponsesGoal, int totalReplicas, Debug debug) {
        this.goal = nResponsesGoal;
        this.totalReplicas = totalReplicas;
        DEBUG = debug;
    }
    
    @Override
    public void onNext(R r) {
        synchronized(this) {
            debug("+++ Adding response " + r);
            this.responses.add(r);
            this.totalResponses++;
        }
    }

    @Override
    public void onError(Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        debug("Status error received: " + status.getCode());

        synchronized(this) {
            this.totalResponses++;
            // Mark connection error for handling after 
            if (status.getCode() == Status.UNAVAILABLE.getCode() 
                || status.getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                
                debug("!!! Server connection error logged.");
                this.replicaDown = true;
                // this check is needed if the last answer is a error (which means onCompleted() is not called)
                if (totalResponses == totalReplicas) {
                    debug("=== End of ");
                    this.notifyAll();
                }

            } else {    // Domain logic exception, stored for re-throw
                debug("=== Server *Logic* error logged. Stored for re-throwing.");
                logicException = status.asRuntimeException();
                this.notifyAll();
            }
        }
    }

    @Override
    public void onCompleted() {
        synchronized(this) {
            if (responses.size() == goal || totalResponses == totalReplicas) {
                debug("=== Finish receiving wanted messages, notify waiting");
                this.notifyAll();
            }
        }
    }

    /**
     * @Warn Only use this inside syncronized block
    **/
    public List<R> getResponses() {
        return responses;
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
	public void debug(Object debugMessage) {
		if (DEBUG == Debug.STRONGER_DEBUG)
			System.err.println(debugMessage);
	}

	public void debugDemo(Object debugMessage) {
		if (DEBUG == Debug.STRONGER_DEBUG || DEBUG == Debug.WEAKER_DEBUG)
			System.err.println(debugMessage);
	}
}