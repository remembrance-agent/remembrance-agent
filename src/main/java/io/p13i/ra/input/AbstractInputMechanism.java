package io.p13i.ra.input;

import io.p13i.ra.utils.StringUtils;

/**
 * Represents a way to place characters on the RA buffer
 */
public abstract class AbstractInputMechanism {

    protected InputMechanismEventsListener inputMechanismEventsListenerCallback;

    /**
     * Gets the name of this class
     *
     * @return the name of this class, readable for the GUI
     */
    public String getInputMechanismName() {
        return StringUtils.splitCamelCase(this.getClass().getSimpleName());
    }

    public void setInputMechanismEventsListenerCallback(InputMechanismEventsListener callback) {
        this.inputMechanismEventsListenerCallback = callback;
    }

    /**
     * Starts the input mechanism
     */
    public abstract void startInputMechanism();

    /**
     * Ends use of this input mechanism
     */
    public abstract void closeInputMechanism();

    /**
     * Used to handle events of the input mechanism
     */
    public interface InputMechanismEventsListener {
        void onInputReady(AbstractInputMechanism inputMechanism);

        /**
         * Callback for input from the input mechanism
         * @param inputMechanism the input mechanism used
         * @param character the character presented (or a special character value)
         * @param isActionKey
         */
        void onInput(AbstractInputMechanism inputMechanism, String character, boolean isActionKey);
    }
}
