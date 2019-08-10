package io.p13i.ra.input;

import io.p13i.ra.utils.LINQList;

import java.util.ArrayList;
import java.util.List;

public class InputMechanismManager {

    private static InputMechanismManager sInstance = new InputMechanismManager();
    private AbstractInputMechanism activeInputMechanism;

    public static InputMechanismManager getInstance() {
        return sInstance;
    }

    private List<AbstractInputMechanism> inputMechanisms = new ArrayList<>();

    public InputMechanismManager addInputMechanism(AbstractInputMechanism inputMechanism) {
        this.inputMechanisms.add(inputMechanism);
        return this;
    }

    public InputMechanismManager initializeAllInputMechanisms() {
        for (AbstractInputMechanism inputMechanism : inputMechanisms) {
            inputMechanism.initalizeInputMechanism();
        }
        return this;
    }

    public InputMechanismManager setOnInputCallbacks(AbstractInputMechanism.OnInput onInputCallback) {
        for (AbstractInputMechanism inputMechanism : inputMechanisms) {
            inputMechanism.setOnInputCallback(onInputCallback);
        }
        return this;
    }

    public <T extends AbstractInputMechanism> T getInputMechanismInstance(Class<T> inputMechanismClass) {
        return new LINQList<>(this.inputMechanisms)
            .where(instance -> instance.getClass().equals(inputMechanismClass))
            .select(inputMechanismClass::cast)
            .first();
    }

    public <T extends AbstractInputMechanism> InputMechanismManager setActiveInputMechanism(Class<T> inputMechanismClass) {
        if (this.activeInputMechanism != null) {
            this.activeInputMechanism.closeInputMechanism();
        }

        this.activeInputMechanism = getInputMechanismInstance(inputMechanismClass);
        return this;
    }

    public AbstractInputMechanism getActiveInputMechanism() {
        return this.activeInputMechanism;
    }

    public InputMechanismManager closeAllInputMechanisms() {
        for (AbstractInputMechanism inputMechanism : inputMechanisms) {
            inputMechanism.closeInputMechanism();
        }
        return this;
    }
}
