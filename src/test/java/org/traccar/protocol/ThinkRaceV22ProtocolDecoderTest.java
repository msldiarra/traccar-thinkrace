package org.traccar.protocol;

import org.junit.jupiter.api.Test;
import org.traccar.ProtocolTest;
import org.traccar.config.Config;
import org.traccar.model.Position;

import static org.junit.jupiter.api.Assertions.*;
public class ThinkRaceV22ProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        Config config = new Config();
        //ThinkRaceV22Protocol protocol = new ThinkRaceV22Protocol(config);

        // IMPORTANT: Only inject the DECODER, not the protocol
        ThinkRaceV22ProtocolDecoder decoder = inject(new ThinkRaceV22ProtocolDecoder(null));

        // Sample message (replace with real one)
        // Real sample frame from your logs (IWAP01 GPS message)
        String message =
                "IWAP01251122A1233.8189N00801.0333W000.1155111068.0904100501500008,610,02,223,10552324#";

        Position position = (Position) decoder.decode(null, null, message);
        
        assertNotNull(position);
        assertNotNull(position, "Decoder should return a Position");
        assertTrue(position.getValid(), "Position should be marked as valid");
        // 12° 33.8189' N  =>  12.5636483
        // 8°  1.0333'  W  => -8.0172217
        assertEquals(12.56365, position.getLatitude(), 0.0001);
        assertEquals(-8.01722, position.getLongitude(), 0.0001);
                
        assertEquals(0.185, position.getSpeed(), 0.1);
        assertEquals(68.09, position.getCourse(), 0.1);
    }
}
