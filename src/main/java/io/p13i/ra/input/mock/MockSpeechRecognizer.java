package io.p13i.ra.input.mock;

import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.utils.IntegerUtils;
import io.p13i.ra.utils.LINQList;

import java.util.function.Consumer;

public class MockSpeechRecognizer extends AbstractInputMechanism {

    @Override
    public void initalizeInputMechanism() {

    }

    @Override
    public void startInput() {
        LINQList.range(5)
            .select(IntegerUtils::asCharacter)
            .forEach(onInputCallback::onInput);
    }

    @Override
    public void closeInputMechanism() {

    }
}
