package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidStationException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;

import java.text.DecimalFormat;


public class InfoStationIT extends BaseIT{
	@Test
    public void infoStationSuccessTest() {
		InfoStationRequest request = getInfoStationRequest("ocea");     

		InfoStationResponse response = frontend.infoStation(request);

        DecimalFormat df = new DecimalFormat("#.####");

        assertEquals("Oceanário", response.getName());
		assertEquals(Float.toString(38.7633f), df.format(response.getCoordinates().getLatitude()));
		assertEquals(Float.toString(-9.0950f), df.format(response.getCoordinates().getLongitude()));
		assertEquals(20, response.getNDocks());
		assertEquals(2, response.getReward());
		assertEquals(15, response.getNBicycles());
		assertEquals(0, response.getNPickUps());
		assertEquals(0, response.getNDeliveries());
    }

    @Test
    public void infoStationNoSuchStationTest() {
        InfoStationRequest request = getInfoStationRequest("stat");     
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void infoStationInvalidStationIdTest() {
        InfoStationRequest request = getInfoStationRequest("s");     
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void infoStationEmptyStationTest() {
        InfoStationRequest request = InfoStationRequest.newBuilder().build(); 

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
    }

}
