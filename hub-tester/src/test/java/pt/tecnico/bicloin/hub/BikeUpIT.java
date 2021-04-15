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


public class BikeUpIT extends BaseIT {
	@BeforeEach
	private void init() {
		// the user must have money in order to perform a bikeUp operation
		frontend.doTopUpOperation("alice", 20, "+35191102030");
	}

	@Test
	public void bikeUpSuccessTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -9.3124f, "stao");

		BikeResponse response = frontend.bikeUp(request);

		// BikeResponse should be empty
		assertEquals(BikeResponse.newBuilder().build(), response);	
	}

	@Test
	public void bikeUpNoSuchUserTest() {
		BikeRequest request = getBikeRequest("u", 38.6867f, -9.3124f, "stao");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeUpInvalidLatitudeTest() {
		BikeRequest request = getBikeRequest("alice", 91.6867f, -9.3124f, "stao");

		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeUpInvalidLongitudeTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -199.3124f, "stao");

		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}


	@Test
	public void bikeUpInvalidStationIdTest() {
		BikeRequest request = getBikeRequest("alice", 38.6867f, -9.3124f, "s");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeUpUserTooFarAwayTest() {
		BikeRequest request = getBikeRequest("alice", 88.6867f, -59.3124f, "stao");

		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserTooFarAwayFromStationException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeUpEmptyUserIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();
		
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeUpEmptyStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("alice")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).build();
		
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidStationException().getMessage(), e.getStatus().getDescription());
	}
	
}
