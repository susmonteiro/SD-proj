package pt.tecnico.rec.frontend;

public class RecordFrontendReplicationWrapper extends RecordFrontend {
    private boolean DEBUG = false;
    
    public RecordFrontendReplicationWrapper(String zooHost, int zooPort) {
        // find all replicas

    }

    public RecordFrontendReplicationWrapper(String zooHost, int zooPort, boolean debug) {
        DEBUG = debug;
    }
    
}
