package io.p13i.ra.input;

import io.p13i.ra.RemembranceAgentClient;
import io.p13i.ra.utils.StringUtils;

/**
 * Represents a way to place characters on the RA buffer
 */
public abstract class AbstractInputMechanism {

    InputMechanismEventsListener inputMechanismEventsListenerCallback = RemembranceAgentClient.getInstance();

    /**
     * Gets the name of this class
     *
     * @return the name of this class, readable for the GUI
     */
    public String getInputMechanismName() {
        return StringUtils.splitCamelCase(this.getClass().getSimpleName());
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

        void onInput(AbstractInputMechanism inputMechanism, Character c);
    }
}
