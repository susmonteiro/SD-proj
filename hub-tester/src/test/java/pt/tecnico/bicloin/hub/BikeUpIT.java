package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidUserException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BikeUpIT extends BaseIT {
	@Disabled
	@Test
	public void bikeUpNoSuchUserTest() {
		BikeRequest request = getBikeRequest("u", 38.6867f, -9.3124f, "stao");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
	}
@Disabled
	@Test
	public void bikeUpInvalidLatitudeTest() {
		BikeRequest request = getBikeRequest("carlos", 238.6867f, -9.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpInvalidLongitudeTest() {
		BikeRequest request = getBikeRequest("carlos", 38.6867f, -99.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpInvalidStationIdTest() {
		BikeRequest request = getBikeRequest("carlos", 38.6867f, -9.3124f, "s");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpUserTooFarAwayIdTest() {
		BikeRequest request = getBikeRequest("carlos", 138.6867f, -59.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpEmptyDockTest() {
		BikeRequest request = getBikeRequest("carlos", 38.6867f, -9.3124f, "empt");
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpEmptyUserIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
@Disabled
	@Test
	public void bikeUpEmptyCoordinatesTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setStationId("stao")
				.build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
	@Disabled
	@Test
	public void bikeUpEmptyStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}
	

}
