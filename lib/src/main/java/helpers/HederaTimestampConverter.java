package helpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class HederaTimestampConverter {
	
    public static LocalDateTime convertToDateTime(long hederaTimestamp) {
        // Convert nanoseconds since Unix epoch to seconds and nanoseconds
        long seconds = hederaTimestamp / 1_000_000_000;
        int nanos = (int) (hederaTimestamp % 1_000_000_000);

        // Create Instant from seconds and nanoseconds
        Instant instant = Instant.ofEpochSecond(seconds, nanos);

        // Convert Instant to LocalDateTime using UTC time zone
        LocalDateTime dateTime = instant.atZone(ZoneOffset.UTC).toLocalDateTime();

        return dateTime;
    }

    public static void main(String[] args) {
        // Example Hedera timestamp (nanoseconds since Unix epoch)
        long hederaTimestamp = 1713883028403168003L;

        // Convert to LocalDateTime
        LocalDateTime dateTime = convertToDateTime(hederaTimestamp);

        System.out.println("Hedera Timestamp: " + hederaTimestamp);
        System.out.println("Converted DateTime: " + dateTime);
    }
}
