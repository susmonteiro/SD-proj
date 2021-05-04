package pt.tecnico.rec;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.*;

import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	public static RecordFrontendReplicationWrapper frontend;

	@BeforeAll
	public static void oneTimeSetup () throws IOException, ZKNamingException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String zooHost = testProps.getProperty("server.zkhost");
		final int zooPort = Integer.parseInt(testProps.getProperty("server.zkport"));
		
		frontend = new RecordFrontendReplicationWrapper(zooHost, zooPort, 1);
	}
	
	@AfterAll
	public static void cleanup() {
		frontend.close();
	}

}
