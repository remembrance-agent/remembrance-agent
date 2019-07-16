package io.p13i.ra.utils;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Complicated... this object manages a size-limited queue of characters.
 * Maintains the last given amount of characters and after a certain amount of time between insertions adds a ' '
 * (space) character automatically.
 */
public class KeyboardLoggerBreakingBuffer {
    private static final Logger LOGGER = LoggerUtils.getLogger(KeyboardLoggerBreakingBuffer.class);
    private static final long BREAKING_BUFFER_DURATION_SEC = 5;  // seconds
    private static final char DEFAULT_BREAKER_CHARACTER = '␣';
    private final LimitedCapacityBuffer<TimestampedCharacter> limitedCapacityBuffer;

    public KeyboardLoggerBreakingBuffer(int maximumCapacity) {
        this.limitedCapacityBuffer = new LimitedCapacityBuffer<>(maximumCapacity);
    }

    /**
     * Adds the given character and a space if required
     * @param c the character to add
     */
    public void addCharacter(char c) {

        if (c == '⌫') {
            limitedCapacityBuffer.removeLast();
            return;
        }

        if (!isCharacterAllowedIntoBuffer(c)) {
            return;
        }

        TimestampedCharacter newTimestamp = new TimestampedCharacter(c);

        // There must already be an element...
        if (limitedCapacityBuffer.size() > 0) {
            TimestampedCharacter lastTimestampedCharacter = limitedCapacityBuffer.getLastAddedElement();
            // This must not be the first element added
            if (lastTimestampedCharacter != null) {
                long keystrokeDelta = DateUtils.deltaSeconds(lastTimestampedCharacter.timestamp, newTimestamp.timestamp);
                LOGGER.info("Keystroke delta: " + keystrokeDelta);
                // And, finally, the delta should be long enough
                if (keystrokeDelta > BREAKING_BUFFER_DURATION_SEC) {
                    LOGGER.info("Added space after " + BREAKING_BUFFER_DURATION_SEC + " seconds.");
                    limitedCapacityBuffer.add(new TimestampedCharacter(DEFAULT_BREAKER_CHARACTER));
                }
            }
        }

        if (c == '␣') {
            newTimestamp.character = DEFAULT_BREAKER_CHARACTER;
        }

        // Add a new time stamp too
        limitedCapacityBuffer.add(newTimestamp);
    }

    private static boolean isCharacterAllowedIntoBuffer(char c) {
        return CharacterUtils.isAlphanumeric(c) || c == '␣' || c == '.' || c == '\'';
    }

    public boolean isEmpty() { return limitedCapacityBuffer.isEmpty(); }

    public void clear() {
        limitedCapacityBuffer.clear();
    }

    public int getTotalTypedCharactersCount() {
        return this.limitedCapacityBuffer.getTotalAddedElementsCount();
    }

    @Override
    public String toString() {
        return this.limitedCapacityBuffer.toString();
    }

    class TimestampedCharacter {
        Character character;
        final Date timestamp;

        TimestampedCharacter(Character c) {
            this.character = c;
            this.timestamp = DateUtils.now();
        }

        @Override
        public String toString() {
            return character.toString();
        }
    }
}
