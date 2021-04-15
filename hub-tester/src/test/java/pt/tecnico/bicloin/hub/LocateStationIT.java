package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNIMPLEMENTED;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;

import java.util.ArrayList;
import java.util.List;


public class LocateStationIT extends BaseIT{
	@Test
    public void LocateStationSuccessTest() {
		LocateStationRequest request = getLocateStationRequest(38.7369f, -9.1366f, 1);   

		LocateStationResponse response = frontend.locateStation(request);

        List<String> stations = new ArrayList<>();
        stations.add("ista");

        assertEquals(stations, response.getStationIdList());
    }

    @Test
    public void LocateStationMultipleSuccessTest() {
		LocateStationRequest request = getLocateStationRequest(38.7633f,-9.0953f, 3);   

		LocateStationResponse response = frontend.locateStation(request);

        List<String> stations = new ArrayList<>();
        stations.add("full");
        stations.add("empt");
        stations.add("ocea");

        assertEquals(stations, response.getStationIdList());
    }

    @Disabled
    @Test
    public void LocateStationNumberSuccessTest() {
		LocateStationRequest request = getLocateStationRequest(38.7369f, -9.1366f, 20);   

		LocateStationResponse response = frontend.locateStation(request);

        assertEquals(11, response.getStationIdList().size());
    }

    @Disabled
    @Test
    public void LocateStationInvalidNumberTest() {
		LocateStationRequest request = getLocateStationRequest(38.7369f, -9.1366f, -1);   

        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.locateStation(request))
            .getStatus().getCode()
        );
    }
    
    @Disabled
    @Test
    public void LocateStationInvalidLatitudeTest() {
		LocateStationRequest request = getLocateStationRequest(238.7633f, -9.0953f, 3);   
        
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.locateStation(request))
            .getStatus().getCode()
        );
    }

}
