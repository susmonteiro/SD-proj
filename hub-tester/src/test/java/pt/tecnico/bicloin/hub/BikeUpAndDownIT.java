package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.NoBikeAvailableException;
import pt.tecnico.bicloin.hub.domain.exception.NotEnoughMoneyException;
import pt.tecnico.bicloin.hub.domain.exception.UserAlreadyOnBikeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BikeUpAndDownIT extends BaseIT {
	@BeforeEach
	private void init() {
		// the user must have money in order to perform a bikeUp operation
		frontend.doTopUpOperation("carlos", 10, "+34203040");
	}

	@Disabled
	@Test
	public void bikeUpAndBikeNoMoneyTest() {
	
		// takes a bicycle
		frontend.doBikeUpOperation("carlos", 38.6867f, -9.3124f, "stao");

		// checks balance
		AmountResponse response = frontend.doBalanceOperation("carlos");
		assertEquals(0, response.getBalance());

		// puts the bicycle in other station
		frontend.doBikeDownOperation("carlos", 38.7372f, -9.3023f, "istt");

		// checks reward was added to user account
		response = frontend.doBalanceOperation("carlos");
		assertEquals(4, response.getBalance());

		// tries another bike up, but has no money
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("carlos", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NoBikeAvailableException().getMessage(), e.getStatus().getDescription());
	}

	@Disabled
	@Test
	public void bikeUpAndBikeDownBicyclesChecksTest() {
		// tries bike up in empty station
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("carlos", 38.6867f, -9.3124f, "empt"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NotEnoughMoneyException().getMessage(), e.getStatus().getDescription());

		// takes a bicycle
		frontend.doBikeUpOperation("carlos", 38.6867f, -9.3124f, "stao");

		// tries to take another bicycle
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("carlos", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserAlreadyOnBikeException().getMessage(), e.getStatus().getDescription());

		// tries to leave bicycle on full station
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("carlos", 38.6867f, -9.3124f, "full"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserAlreadyOnBikeException().getMessage(), e.getStatus().getDescription());

		// leaves bicycle on another station
		frontend.doBikeDownOperation("carlos", 38.6867f, -9.3124f, "stao");

		// tries to bike down a bicycle again
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("carlos", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserAlreadyOnBikeException().getMessage(), e.getStatus().getDescription());
	}



	@Disabled
	@Test
	public void bikeDownSuccessTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();

		BikeResponse response = frontend.bikeDown(request);

		assertEquals("", response);	// BikeResponse should be empty
	}

}
