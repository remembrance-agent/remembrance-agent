package io.p13i.ra.input;

public abstract class AbstractInputMechanism {

    protected OnInput onInputCallback;

    public final void setOnInputCallback(OnInput onInputCallback) {
        this.onInputCallback = onInputCallback;
    }

    public abstract void startInput();
    public abstract void endInput();

    public interface OnInput {
        public void onInput(Character c);
    }
}
