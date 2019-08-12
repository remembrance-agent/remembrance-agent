package io.p13i.ra;

import com.google.inject.AbstractModule;
import io.p13i.ra.gui.GUI;
import io.p13i.ra.utils.TFIDFCalculator;

public class RemembranceAgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GUI.class).toInstance(new GUI());
    }
}
