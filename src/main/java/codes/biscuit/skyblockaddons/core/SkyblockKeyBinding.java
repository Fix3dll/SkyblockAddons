package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Locale;

@Getter
public enum SkyblockKeyBinding {
    OPEN_SETTINGS(Keyboard.KEY_NONE, "settings.settings"),
    OPEN_EDIT_GUI(Keyboard.KEY_NONE, "settings.editLocations"),
    LOCK_SLOT(Keyboard.KEY_L, "settings.lockSlot"),
    FREEZE_BACKPACK(Keyboard.KEY_F, "settings.freezeBackpackPreview"),
    INCREASE_DUNGEON_MAP_ZOOM(Keyboard.KEY_ADD, "keyBindings.increaseDungeonMapZoom"),
    DECREASE_DUNGEON_MAP_ZOOM(Keyboard.KEY_SUBTRACT, "keyBindings.decreaseDungeonMapZoom"),
    DEVELOPER_COPY_NBT(Minecraft.isRunningOnMac ? Keyboard.KEY_LMENU : Keyboard.KEY_RCONTROL, "keyBindings.developerCopyNBT");

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private final int defaultKeyCode;
    private final String translationKey;
    private final KeyBinding keyBinding;

    private boolean registered = false;
    private boolean isFirstRegistration = true;
    /**
     * This is the key code stored before the key binding is de-registered
     * It's set to a number larger than Keyboard.KEYBOARD_SIZE by default to indicate no previous key code is stored.
     */
    private int previousKeyCode = 999;

    SkyblockKeyBinding(int defaultKey, String translationKey) {
        this.defaultKeyCode = defaultKey;
        this.translationKey = translationKey;
        this.keyBinding = new KeyBinding("key.skyblockaddons." + this.name().toLowerCase(Locale.US), defaultKey, SkyblockAddons.MOD_NAME);
    }

    /**
     * Returns the current key code for this key binding.
     * @return the current key code for this key binding
     */
    public int getKeyCode() {
        int keyCode = keyBinding.getKeyCode();
        return keyCode == 0 ? Integer.MAX_VALUE : keyCode;
    }

    /**
     * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
     * @see KeyBinding#isKeyDown()
     */
    public boolean isKeyDown() {
        if (registered) {
            int keyCode = this.getKeyCode();

            if (keyCode < 0) {
                return Mouse.isButtonDown(keyCode + 100);
            } else {
                return Keyboard.isKeyDown(keyCode);
            }
        } else {
            return false;
        }
    }

    /**
     * Returns true on the initial key press. For continuous querying use {@link SkyblockKeyBinding#isKeyDown()}. Should be used in key
     * events.
     * @see KeyBinding#isPressed()
     */
    public boolean isPressed() {
        if (registered) {
            int keyCode = this.getKeyCode();

            if (keyCode < 0) {
                return Mouse.getEventButtonState() && Mouse.getEventButton() == keyCode + 100;
            } else {
                return Keyboard.getEventKeyState() && Keyboard.getEventKey() == keyCode;
            }
        } else {
            return false;
        }
    }

    /**
     * Adds this keybinding to {@link Minecraft#gameSettings}. If the key binding is not being registered for the first
     * time, its previous keycode setting from before its last de-registration is restored.
     */
    public void register() {
        if (registered) {
            LOGGER.error("Tried to register a key binding with the name \"{}\" which is already registered.", this.name().toLowerCase(Locale.US));
            return;
        }

        ClientRegistry.registerKeyBinding(keyBinding);

        if (isFirstRegistration) {
            isFirstRegistration = false;
        } else if (previousKeyCode < Keyboard.KEYBOARD_SIZE) {
            keyBinding.setKeyCode(previousKeyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
        }

        registered = true;
    }

    /**
     * Removes this keybinding from {@link Minecraft#gameSettings}.
     */
    public void deRegister() {
        if (registered) {
            int index = ArrayUtils.indexOf(Minecraft.getMinecraft().gameSettings.keyBindings, keyBinding);

            if (index == ArrayUtils.INDEX_NOT_FOUND) {
                LOGGER.error("Keybinding was registered but no longer exists in the registry. "
                        + "Something else must have removed it. "
                        + "This shouldn't happen; please inform an SBA developer.");
                registered = false;
                return;
            }

            Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.remove(Minecraft.getMinecraft().gameSettings.keyBindings, index);

            /*
            The key binding still exists in the internal list even though it's removed from the settings menu.
            We have to set its key to KEY_NONE so it does not conflict with other key bindings.
             */
            previousKeyCode = keyBinding.getKeyCode();
            keyBinding.setKeyCode(Keyboard.KEY_NONE);
            KeyBinding.resetKeyBindingArrayAndHash();
            registered = false;
        } else {
            LOGGER.error("Tried to de-register a key binding with the name \"{}\" which wasn't registered.", this.name().toLowerCase(Locale.US));
        }
    }

    /**
     * Registers the given keybindings to the {@link net.minecraftforge.fml.client.registry.ClientRegistry}.
     */
    public static void registerAllKeyBindings() {
        for (SkyblockKeyBinding keybinding: SkyblockKeyBinding.values()) {
            keybinding.register();
        }
    }

}