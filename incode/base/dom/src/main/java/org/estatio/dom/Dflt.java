package org.estatio.dom;

import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;

public class Dflt {

    private Dflt(){}

    @Programmatic
    public static <T> T of(final List<T> choices) {
        return choices.size() == 1? choices.get(0): null;
    }
}
