/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.misc.ChineseTranslations;

public abstract class WLabel extends WPressable {
    public Color color;

    protected String text;
    private String sourceText;
    protected boolean title;

    public WLabel(String text, boolean title) {
        this.sourceText = text;
        this.text = ChineseTranslations.text(text);
        this.title = title;
    }

    @Override
    protected void onCalculateSize() {
        width = theme.textWidth(text, text.length(), title);
        height = theme.textHeight(title);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (action != null) return super.onMouseClicked(mouseX, mouseY, button, used);
        return false;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (action != null) return super.onMouseReleased(mouseX, mouseY, button);
        return false;
    }

    public void set(String text) {
        this.sourceText = text;
        text = ChineseTranslations.text(text);
        if (Math.round(theme.textWidth(text, text.length(), title)) != width) invalidate();

        this.text = text;
    }

    public String get() {
        return sourceText;
    }

    public WLabel color(Color color) {
        this.color = color;
        return this;
    }
}
