package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BikeUpAndDownIT extends BaseIT {
	@Disabled
	@Test
	public void bikeUpSuccessTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();

		BikeResponse response = frontend.bikeUp(request);

		assertEquals("", response);	// BikeResponse should be empty
	}
	@Disabled
	@Test
	public void bikeUpCannotHave2BikesTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
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
	
	@Disabled
	@Test
	public void bikeDownNoBikeOnTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("stao")
				.build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Disabled
	@Test
	public void bikeDownFullDockTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("full")
				.build();

		// Bike User must have a bike
		BikeResponse response = frontend.bikeUp(request);

		assertEquals("", response);	// BikeResponse should be empty
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

}
