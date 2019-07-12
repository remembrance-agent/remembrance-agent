package io.p13i.ra.utils;

import java.util.Date;
import java.util.logging.Logger;

public class KeyboardLoggerBreakingBuffer {
    private static final Logger LOGGER = Logger.getLogger(KeyboardLoggerBreakingBuffer.class.getName());
    private static final long BREAKING_BUFFER_DURATION_SEC = 10;  // seconds
    private LimitedCapacityBuffer<TimestampedCharacter> limitedCapacityBuffer;
    public KeyboardLoggerBreakingBuffer(int maximumCapacity) {
        this.limitedCapacityBuffer = new LimitedCapacityBuffer<>(maximumCapacity);
    }

    public void addCharacter(char c) {
        TimestampedCharacter newTimestamp = new TimestampedCharacter(c);
        /* seconds */
        if (limitedCapacityBuffer.size() > 0) {
            TimestampedCharacter lastTimestampedCharacter = limitedCapacityBuffer.getLastAddedElement();
            if (lastTimestampedCharacter != null) {
                long keystrokeDelta = DateUtils.deltaSeconds(lastTimestampedCharacter.timestamp, newTimestamp.timestamp);
                LOGGER.info("Keystroke delta: " + keystrokeDelta);
                if (keystrokeDelta > BREAKING_BUFFER_DURATION_SEC) {
                    LOGGER.info("Added space after " + BREAKING_BUFFER_DURATION_SEC + " seconds.");
                    limitedCapacityBuffer.add(new TimestampedCharacter(' '));
                }
            }
        }
        limitedCapacityBuffer.add(newTimestamp);
    }

    @Override
    public String toString() {
        return this.limitedCapacityBuffer.toString();
    }

    class TimestampedCharacter {
        Character character;
        Date timestamp;
        public TimestampedCharacter(Character c) {
            this.character = c;
            this.timestamp = DateUtils.now();
        }

        @Override
        public String toString() {
            return character.toString();
        }
    }
}
