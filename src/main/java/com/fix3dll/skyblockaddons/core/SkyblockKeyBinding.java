package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.accessors.GameOptionsAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Getter
public enum SkyblockKeyBinding {
    OPEN_SETTINGS(GLFW.GLFW_KEY_UNKNOWN, "settings.settings"),
    OPEN_EDIT_GUI(GLFW.GLFW_KEY_UNKNOWN, "settings.editLocations"),
    LOCK_SLOT(GLFW.GLFW_KEY_L, "settings.lockSlot"),
    FREEZE_BACKPACK(GLFW.GLFW_KEY_F, "settings.freezeBackpackPreview"),
    INCREASE_DUNGEON_MAP_ZOOM(GLFW.GLFW_KEY_KP_ADD, "keyBindings.increaseDungeonMapZoom"),
    DECREASE_DUNGEON_MAP_ZOOM(GLFW.GLFW_KEY_KP_SUBTRACT, "keyBindings.decreaseDungeonMapZoom"),
    ANSWER_ABIPHONE_OR_OPTION(GLFW.GLFW_KEY_UNKNOWN, "keyBindings.answerAbiphoneOrOption"),
    DEVELOPER_COPY_NBT(Util.getPlatform() == Util.OS.OSX? GLFW.GLFW_KEY_LEFT_ALT : GLFW.GLFW_KEY_RIGHT_CONTROL, "keyBindings.developerCopyNBT");

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private final InputConstants.Key defaultKey;
    private final String translationKey;
    private final KeyMapping keyBinding;

    private boolean registered = false;
    private boolean isFirstRegistration = true;
    /**
     * This is the key code stored before the key binding is de-registered.
     * It's set to a number larger than Keyboard.KEYBOARD_SIZE by default to indicate no previous key code is stored.
     */
    private InputConstants.Key previousKey = InputConstants.UNKNOWN;

    SkyblockKeyBinding(int defaultKeyCode, String translationKey) {
        this.defaultKey = InputConstants.getKey(defaultKeyCode, -1);
        this.translationKey = translationKey;
        String key = "key.skyblockaddons." + this.name().toLowerCase(Locale.US);
        this.keyBinding = new KeyMapping(key, defaultKeyCode, SkyblockAddons.METADATA.getName());
    }

    /**
     * Returns the current key code for this key binding.
     * @return the current key code for this key binding
     */
    public int getKeyCode() {
        return keyBinding.key.getValue();
    }

    /**
     * Returns {@code true} on the initial key press. For continuous querying use {@link SkyblockKeyBinding#isDown()}. Should be used in key events.
     * @see KeyMapping#consumeClick()
     */
    public boolean consumeClick() {
        if (registered) {
            return keyBinding.consumeClick();
        } else {
            return false;
        }
    }

    /**
     * Returns {@code true} if the key is pressed (used for continuous querying). Should be used in tickers.
     * @see KeyMapping#isDown()
     */
    public boolean isDown() {
        if (registered) {
            return keyBinding.isDown();
        } else {
            return false;
        }
    }

    /**
     * Adds this keybinding to {@link net.minecraft.client.Minecraft#options}. If the key binding is not being registered for the first
     * time, its previous keycode setting from before its last de-registration is restored.
     */
    public void register(Options options) {
        if (registered) {
            LOGGER.error("Tried to register a key binding with the name \"{}\" which is already registered.", this.name().toLowerCase(Locale.US));
            return;
        }

        GameOptionsAccessor accessor = (GameOptionsAccessor) options;
        accessor.sba$updateAllKeys(ArrayUtils.add(accessor.sba$getAllKeys(), keyBinding));

        if (isFirstRegistration) {
            isFirstRegistration = false;
        } else if (previousKey != InputConstants.UNKNOWN) {
            keyBinding.setKey(previousKey);
        }

        registered = true;
    }

    /**
     * Removes this keybinding from {@link Minecraft#options}.
     */
    public void deRegister() {
        if (registered) {
            GameOptionsAccessor accessor = (GameOptionsAccessor) Minecraft.getInstance().options;
            int index = ArrayUtils.indexOf(accessor.sba$getAllKeys(), keyBinding);

            if (index == ArrayUtils.INDEX_NOT_FOUND) {
                LOGGER.error("Keybinding was registered but no longer exists in the registry. "
                        + "Something else must have removed it. "
                        + "This shouldn't happen; please inform an SBA developer.");
                registered = false;
                return;
            }

            accessor.sba$updateAllKeys(ArrayUtils.remove(accessor.sba$getAllKeys(), index));

            // The key binding still exists in the internal list even though it's removed from the settings menu.
            // We have to set its key to KEY_NONE so it does not conflict with other key bindings.
            previousKey = keyBinding.key;
            keyBinding.setKey(InputConstants.UNKNOWN);
            registered = false;
        } else {
            LOGGER.error("Tried to de-register a key binding with the name \"{}\" which wasn't registered.", this.name().toLowerCase(Locale.US));
        }
    }

    /**
     * Registers the all keybindings.
     */
    public static void registerAllKeyBindings(Options options) {
        addCategory(SkyblockAddons.METADATA.getName());
        for (SkyblockKeyBinding keybinding: SkyblockKeyBinding.values()) {
            if (keybinding.isFirstRegistration()) {
                keybinding.register(options);
            }
        }
    }

    private static void addCategory(String categoryTranslationKey) {
        Map<String, Integer> map = KeyMapping.CATEGORY_SORT_ORDER;

        if (map.containsKey(categoryTranslationKey)) {
            return;
        }

        Optional<Integer> largest = map.values().stream().max(Integer::compareTo);
        int largestInt = largest.orElse(0);
        map.put(categoryTranslationKey, largestInt + 1);
    }

}