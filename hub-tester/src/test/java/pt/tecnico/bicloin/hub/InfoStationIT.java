package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNIMPLEMENTED;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class InfoStationIT extends BaseIT{	
	@Test
    public void infoStationSuccessTest() {
		InfoStationRequest request = InfoStationRequest.newBuilder()
		.setStationId("ocea")
		.build();     

		InfoStationResponse response = frontend.infoStation(request);

        assertEquals("Oceanário", response.getName());
		assertEquals(38.7633, response.getCoordinates().getLatitude());
		assertEquals(-9.0950, response.getCoordinates().getLongitude());
		assertEquals(20, response.getNDocks());
		assertEquals(2, response.getReward());
		assertEquals(15, response.getNBicycles());
		assertEquals(0, response.getNPickUps());
		assertEquals(0, response.getNDeliveries());
    }

    @Test
    public void infoStationNoSuchStationTest() {
        InfoStationRequest request = InfoStationRequest.newBuilder()
		    .setStationId("s")
		    .build();          
        assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request))
            .getStatus().getCode()
        );
    }

    @Test
    public void infoStationEmptyStationTest() {
        InfoStationRequest request = InfoStationRequest.newBuilder().build();          
        assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request))
            .getStatus().getCode()
        );
    }

}
