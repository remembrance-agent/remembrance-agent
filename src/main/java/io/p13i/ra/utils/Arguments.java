package io.p13i.ra.utils;

public class Arguments {
    /**
     * Exception for null argument
     */
    public static class NullArgumentException extends IllegalArgumentException {
        public NullArgumentException(String argumentName) {
            super(argumentName);
        }
    }

    public static class Ensure {
        /**
         * Ensures that the given argument is not null
         * @param argument The argument value
         * @param name The name of the argument
         * @throws NullArgumentException If the argument is null
         */
        public static void NotNull(Object argument, String name) {
            if (argument == null) {
                throw new NullArgumentException(name != null ? name : "null");
            }
        }

        /**
         * Ensures that all provided arguments are not null
         * @param arguments Method arguments
         * @throws NullArgumentException If an argument is null
         */
        public static void NotNull(Object... arguments) {
            for (int i = 0; i < arguments.length; i++) {
                Object arg = arguments[i];
                Ensure.NotNull(arg, "arguments[" + i + "]");
            }
        }
    }
}
