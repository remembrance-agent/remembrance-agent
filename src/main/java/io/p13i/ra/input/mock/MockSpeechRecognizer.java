package io.p13i.ra.input.mock;

import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.utils.CharacterUtils;
import io.p13i.ra.utils.LINQList;

public class MockSpeechRecognizer extends AbstractInputMechanism {

    @Override
    public void startInput() {
        LINQList.from("Hello from a mock speech recognition engine")
            .select(CharacterUtils::toUpperCase)
            .forEach(inputEventsListenerCallback::onInput);
    }

    @Override
    public void closeInputMechanism() {

    }
}
