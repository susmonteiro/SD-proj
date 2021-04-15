package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.UNIMPLEMENTED;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class BikeDownIT extends BaseIT {
	@Test
	public void bikeDownNoSuchUserTest() {
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
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownInvalidLatitudeTest() {
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
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownInvalidLongitudeTest() {
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
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownInvalidStationIdTest() {
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
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void bikeDownUserTooFarAwayIdTest() {
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
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
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
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}

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
	
	@Test
	public void bikeDownEmptyStationIdTest() {
		BikeRequest request = BikeRequest.newBuilder()
				.setUserId("carlos")
				.setCoordinates(Coordinates.newBuilder()
					.setLatitude(38.6867f)
					.setLongitude(-9.3124f)
					.build()
				).build();
		
		assertEquals(
            UNIMPLEMENTED.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request))
            .getStatus().getCode()
        );
	}
	

}
