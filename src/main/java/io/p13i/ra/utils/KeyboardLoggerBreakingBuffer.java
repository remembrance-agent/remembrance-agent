package io.p13i.ra.utils;

import java.util.Date;

/**
 * Complicated... this object manages a size-limited queue of characters.
 * Maintains the last given amount of characters and after a certain amount of time between insertions adds a ' '
 * (space) character automatically.
 */
public class KeyboardLoggerBreakingBuffer {
    private static final long BREAKING_BUFFER_DURATION_SEC = 2;  // seconds
    private static final char DEFAULT_BREAKER_CHARACTER = '␣';
    private final LimitedCapacityBuffer<TimestampedCharacter> limitedCapacityBuffer;

    public KeyboardLoggerBreakingBuffer(int maximumCapacity) {
        this.limitedCapacityBuffer = new LimitedCapacityBuffer<>(maximumCapacity);
    }

    /**
     * Adds the given character and a space if required
     *
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
                // And, finally, the delta should be long enough
                if (keystrokeDelta > BREAKING_BUFFER_DURATION_SEC) {
                    limitedCapacityBuffer.add(new TimestampedCharacter(DEFAULT_BREAKER_CHARACTER));
                }
            }
        }

        if (CharacterUtils.isSpace(c)) {
            newTimestamp.character = DEFAULT_BREAKER_CHARACTER;
        }

        // Add a new time stamp too
        limitedCapacityBuffer.add(newTimestamp);
    }

    /**
     * Determines if a character is allowed in to the buffer
     *
     * @param c the character to check
     * @return whether or not the character is allowed in the buffer
     */
    private static boolean isCharacterAllowedIntoBuffer(char c) {
        return CharacterUtils.isAlphanumeric(c) || CharacterUtils.isSpace(c) || c == '.' || c == '\'' || c == '-';
    }

    /**
     * Empties the buffer
     */
    public void clear() {
        limitedCapacityBuffer.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TimestampedCharacter timestampedCharacter : this.limitedCapacityBuffer) {
            stringBuilder.append(timestampedCharacter.character);
        }
        return stringBuilder.toString();
    }

    /**
     * Represents a character and tht time it was initialized
     */
    private class TimestampedCharacter {
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
