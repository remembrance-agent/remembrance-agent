package io.p13i.ra.input;

import io.p13i.ra.RemembranceAgentClient;
import io.p13i.ra.utils.StringUtils;

public abstract class AbstractInputMechanism {

    protected InputEventsListener inputEventsListenerCallback = RemembranceAgentClient.getInstance();

    public String getInputMechanismName() {
        return StringUtils.splitCamelCase(this.getClass().getSimpleName());
    }

    public abstract void startInput();
    public abstract void closeInputMechanism();

    public interface InputEventsListener {
        void onInputReady(AbstractInputMechanism inputMechanism);
        void onInput(Character c);
    }
}
