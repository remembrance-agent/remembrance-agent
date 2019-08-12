package io.p13i.ra;

import com.google.inject.AbstractModule;
import io.p13i.ra.gui.GUI;

public class RemembranceAgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GUI.class).toInstance(new GUI());
    }
}
