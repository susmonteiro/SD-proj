package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidStationException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidUserException;
import pt.tecnico.bicloin.hub.domain.exception.UserTooFarAwayFromStationException;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BikeDownIT extends BaseIT {
	@Test
	public void bikeDownNoSuchUserTest() {
		BikeRequest request = getBikeRequest("u", 38.6867f, -9.3124f, "stao");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
		
	}

	@Test
	public void bikeDownInvalidLatitudeTest() {
		BikeRequest request = getBikeRequest("alice", 238.6867f, -9.3124f, "stao");

		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownInvalidLongitudeTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -199.3124f, "stao");

		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownInvalidStationIdTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -9.3124f, "s");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeDownUserTooFarAwayIdTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -59.3124f, "stao");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserTooFarAwayFromStationException().getMessage(), e.getStatus().getDescription());
	}

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
	public void bikeDownEmptyStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("alice")
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
