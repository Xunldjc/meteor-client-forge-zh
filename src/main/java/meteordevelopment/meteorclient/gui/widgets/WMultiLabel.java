/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.utils.misc.TextWrapping;

public abstract class WMultiLabel extends WLabel {
    protected List<String> lines = new ArrayList<>(2);

    protected double maxWidth;

    public WMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title);

        this.maxWidth = maxWidth;
    }

    @Override
    protected void onCalculateSize() {
        lines.clear();

        lines.addAll(TextWrapping.wrap(text, theme.scale(maxWidth),
            value -> theme.textWidth(value, value.length(), title)));
        width = lines.stream().mapToDouble(value -> theme.textWidth(value, value.length(), title)).max().orElse(0);
        height = theme.textHeight(title) * lines.size();
    }

    @Override
    public void set(String text) {
        if (!text.equals(get())) invalidate();
        super.set(text);
    }
}
