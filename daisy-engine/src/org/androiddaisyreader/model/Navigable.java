package org.androiddaisyreader.model;

import java.util.List;

public interface Navigable {
    List<? extends Navigable> getChildren();
}
