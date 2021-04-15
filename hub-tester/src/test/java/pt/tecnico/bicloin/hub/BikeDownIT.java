package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidStationException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidUserException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BikeDownIT extends BaseIT {
	@Disabled
	@Test
	public void bikeDownNoSuchUserTest() {
		BikeRequest request = getBikeRequest("u", 38.6867f, -9.3124f, "stao");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
		
	}

	@Disabled
	@Test
	public void bikeDownInvalidLatitudeTest() {
		BikeRequest request = getBikeRequest("alice", 238.6867f, -9.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Disabled
	@Test
	public void bikeDownInvalidLongitudeTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -99.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Disabled
	@Test
	public void bikeDownInvalidStationIdTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -9.3124f, "s");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
	}

	@Disabled
	@Test
	public void bikeDownUserTooFarAwayIdTest() {
		BikeRequest request = getBikeRequest("alice", 138.6867f, -59.3124f, "stao");

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Disabled
	@Test
	public void bikeDownEmptyUserIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();
		
				StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
				assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
				assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
	}

	@Disabled
	@Test
	public void bikeDownEmptyCoordinatesTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setStationId("stao")
				.build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}
	
	@Disabled
	@Test
	public void bikeDownEmptyStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).build();
		
				StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
				assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
				assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
	}
	

}
