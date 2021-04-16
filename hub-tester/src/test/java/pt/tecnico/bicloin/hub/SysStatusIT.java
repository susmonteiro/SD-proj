package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class SysStatusIT extends BaseIT {
    @Test
    public void sysStatusTest() {
        // check that sysStatus does not send an exception
        SysStatusRequest request = getSysStatusRequest();
        SysStatusResponse response = frontend.sysStatus(request);
        assertNotEquals(SysStatusResponse.newBuilder().build(), response);
    }
}
