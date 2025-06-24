package com.fix3dll.skyblockaddons.gui.buttons;

@FunctionalInterface
public interface UpdateCallback<T> {

    void onUpdate(T updatedValue);
}
