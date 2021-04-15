package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class BikeUpIT extends BaseIT {
	@Test
	public void bikeUpNoSuchUserTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("u")
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

	@Test
	public void bikeUpInvalidLatitudeTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(238.6867f)
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

	@Test
	public void bikeUpInvalidLongitudeTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-99.3124f)
					.build()
				).setStationId("stao")
				.build();

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeUpInvalidStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("s")
				.build();

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeUpUserTooFarAwayIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(138.6867f)
					.setLongitude(-59.3124f)
					.build()
				).setStationId("stao")
				.build();

		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeUpEmptyDockTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).setStationId("empt")
				.build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
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
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request))
            .getStatus().getCode()
        );
	}

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
