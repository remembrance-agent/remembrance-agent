package io.p13i.ra.input;

import io.p13i.ra.utils.StringUtils;

public abstract class AbstractInputMechanism {

    protected OnInput onInputCallback;

    public final void setOnInputCallback(OnInput onInputCallback) {
        this.onInputCallback = onInputCallback;
    }
    public String getInputMechanismName() {
        return StringUtils.splitCamelCase(this.getClass().getSimpleName());
    }

    public abstract void initalizeInputMechanism();
    public abstract void startInput();
    public abstract void closeInputMechanism();

    public interface OnInput {
        void onInput(Character c);
    }
}
