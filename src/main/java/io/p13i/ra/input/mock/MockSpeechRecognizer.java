package io.p13i.ra.input.mock;

import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.utils.CharacterUtils;
import io.p13i.ra.utils.IntegerUtils;
import io.p13i.ra.utils.LINQList;

import java.util.function.Consumer;

public class MockSpeechRecognizer extends AbstractInputMechanism {

    @Override
    public void initalizeInputMechanism() {

    }

    @Override
    public void startInput() {
        LINQList.from("Hello from a mock speech recognition engine")
            .select(CharacterUtils::toUpperCase)
            .forEach(onInputCallback::onInput);
    }

    @Override
    public void closeInputMechanism() {

    }
}
