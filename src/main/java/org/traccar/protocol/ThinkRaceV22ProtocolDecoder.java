package org.traccar.protocol;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.traccar.helper.DateBuilder;

import java.util.Date;

import io.netty.channel.Channel;
import java.net.SocketAddress;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Protocol;
import org.traccar.model.Position;

public class ThinkRaceV22ProtocolDecoder extends BaseProtocolDecoder {

    public ThinkRaceV22ProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;
        sentence = sentence.trim();

        if (sentence.startsWith("IWAP01")) {

            // Example:
            // IWAP01 251122 A 1233.8189N 00801.0333W 000.1 155111 068.09 041 005 015 00008,610,02,223,10552324#

            Pattern pattern = Pattern.compile(
                "IWAP01(\\d{2})(\\d{2})(\\d{2})" +    // 1,2,3: date dd mm yy
                "([AV])" +                           // 4: validity
                "(\\d{2})(\\d{2}\\.\\d+)([NS])" +     // 5,6,7: lat dd mm.mmmm N/S
                "(\\d{3})(\\d{2}\\.\\d+)([EW])" +     // 8,9,10: lon ddd mm.mmmm E/W
                "(\\d{3}\\.\\d)" +                    // 11: speed 000.1
                "(\\d{6})" +                          // 12: time hhmmss
                "(\\d{3}\\.\\d{2})"                   // 13: course 068.09
            );

            Matcher matcher = pattern.matcher(sentence);
            if (!matcher.find()) {
                return null;
            }

            Position position = new Position(getProtocolName());

            // --- VALIDITY ---
            boolean valid = "A".equals(matcher.group(4));
            position.setValid(valid);

            // --- DATE ---
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = 2000 + Integer.parseInt(matcher.group(3));

            // --- TIME ---
            String timeStr = matcher.group(12);
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            int second = Integer.parseInt(timeStr.substring(4, 6));

            // Note: DateBuilder does not accept multiple fields in its constructor.// Note: DateBuilder does not accept multiple fields in its constructor.
            // You should use the default constructor and then set fields with setter methods, as below:

            // Example of constructing a date using DateBuilder:
            DateBuilder dateBuilder = new DateBuilder()
                    .setDate(year, month, day)
                    .setTime(hour, minute, second);
            position.setTime(dateBuilder.getDate());

            // --- LATITUDE ---
            double latDeg = Double.parseDouble(matcher.group(5));
            double latMin = Double.parseDouble(matcher.group(6));
            double latitude = latDeg + (latMin / 60.0);
            if ("S".equals(matcher.group(7))) {
                latitude = -latitude;
            }
            position.setLatitude(latitude);

            // --- LONGITUDE ---
            double lonDeg = Double.parseDouble(matcher.group(8));
            double lonMin = Double.parseDouble(matcher.group(9));
            double longitude = lonDeg + (lonMin / 60.0);
            if ("W".equals(matcher.group(10))) {
                longitude = -longitude;
            }
            position.setLongitude(longitude);

            // --- SPEED (knots → knots * 1.852 = km/h) ---
            double speedKnots = Double.parseDouble(matcher.group(11));
            position.setSpeed(speedKnots * 1.852);  // km/h

            // --- COURSE ---
            double course = Double.parseDouble(matcher.group(13));
            position.setCourse(course);

            return position;
        }

        if (!sentence.startsWith("IWAP")) {
            return null;
        }

        if (sentence.endsWith("#")) {
            sentence = sentence.substring(0, sentence.length() - 1);
        }

        int apIndex = sentence.indexOf(",AP1|");
        if (apIndex >= 0) {
            sentence = sentence.substring(0, apIndex);
        }

        String type = sentence.substring(4, 6);

        if ("01".equals(type) || "10".equals(type)) {
            return decodePosition(sentence, channel, remoteAddress);
        } else {
            // other message types (JK, BL, etc.) are ignored for now
            return null;
        }
    }

    private Object decodePosition(
            String sentence, Channel channel, SocketAddress remoteAddress) throws Exception {

        Position position = new Position(getProtocolName());

        // basic placeholder: mark as invalid and return
        position.setValid(false);

        return position;
    }
}
