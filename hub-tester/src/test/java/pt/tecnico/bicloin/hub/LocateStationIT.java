package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNIMPLEMENTED;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class LocateStationIT extends BaseIT{
    // @Disabled	
	// @Test
    // public void LocateStationSuccessTest() {
	// 	LocateStationRequest request = locateStationRequest(38.7633f, -9.0951f, 1);   

	// 	LocateStationResponse response = frontend.locateStation(request);

    //     assertEquals("ocea", response.getStationIdList());
    // }

}
