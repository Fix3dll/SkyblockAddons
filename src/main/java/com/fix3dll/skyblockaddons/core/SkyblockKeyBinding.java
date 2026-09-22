package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.accessors.GameOptionsAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.sdl.SDLMouse;

import java.util.List;
import java.util.Locale;

import static com.fix3dll.skyblockaddons.SkyblockAddons.CATEGORY;

@Getter
public enum SkyblockKeyBinding {
    OPEN_SETTINGS(InputConstants.UNKNOWN.getValue(), Type.KEYBOARD, "settings.settings"),
    OPEN_EDIT_GUI(InputConstants.UNKNOWN.getValue(), Type.KEYBOARD, "settings.editLocations"),
    LOCK_SLOT(InputConstants.KEY_L, Type.KEYBOARD, "settings.lockSlot"),
    FREEZE_BACKPACK(InputConstants.KEY_F, Type.KEYBOARD, "settings.freezeBackpackPreview"),
    INCREASE_DUNGEON_MAP_ZOOM(InputConstants.KEY_ADD, Type.KEYBOARD, "keyBindings.increaseDungeonMapZoom"),
    DECREASE_DUNGEON_MAP_ZOOM(86/*SUBTRACT*/, Type.KEYBOARD, "keyBindings.decreaseDungeonMapZoom"),
    ANSWER_ABIPHONE_OR_OPTION(InputConstants.UNKNOWN.getValue(), Type.KEYBOARD, "keyBindings.answerAbiphoneOrOption"),
    SHOW_BULK_PRICE(InputConstants.KEY_LSHIFT, Type.KEYBOARD, "keyBindings.showBulkPrice"),
    SHOW_MISSING_ENCHANTS(InputConstants.KEY_LSHIFT, Type.KEYBOARD, "keyBindings.showMissingEnchants"),
    DEVELOPER_COPY_NBT(InputConstants.KEY_RCONTROL, Type.KEYBOARD, "keyBindings.developerCopyNBT");

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

    SkyblockKeyBinding(int defaultKey, Type type, String translationKey) {
        this.defaultKey = type.getOrCreate(defaultKey);
        this.translationKey = translationKey;
        String key = "key.skyblockaddons." + this.name().toLowerCase(Locale.ENGLISH);
        this.keyBinding = new KeyMapping(key, type, defaultKey, CATEGORY);
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

    public boolean isKeyDown() {
        int keyCode = this.getKeyCode();

        if (keyCode == 0) {
            return false;
        } else if (1 <= keyCode && keyCode <= 8) {
            return (SDLMouse.SDL_GetMouseState(null, null) & (1 << (keyCode - 1))) != 0;
        } else {
            return InputConstants.isKeyDown(keyCode);
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
        addCategory();
        for (SkyblockKeyBinding keybinding: SkyblockKeyBinding.values()) {
            if (keybinding.isFirstRegistration()) {
                keybinding.register(options);
            }
        }
    }

    private static void addCategory() {
        List<KeyMapping.Category> sortOrder = KeyMapping.Category.SORT_ORDER;

        if (!sortOrder.contains(CATEGORY)) {
            sortOrder.addLast(CATEGORY);
        }
    }

}