package com.fix3dll.skyblockaddons.features.backpacks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.listeners.RenderListener;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData.ContainerType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
public class ContainerPreviewManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("skyblockaddons", "containerpreview.png");
    private static final Pattern BACKPACK_STORAGE_PATTERN = Pattern.compile("Backpack Slot (?<slot>\\d+)");
    private static final Pattern ENDERCHEST_STORAGE_PATTERN = Pattern.compile("Ender Chest Page (?<page>\\d+)");

    /**
     * The container preview to render
     */
    private static ContainerPreview currentContainerPreview;
    private static UUID cachedBackpackUuid;
    private static ContainerPreview cachedContainerPreview;

    /**
     * The current container inventory that will be saved to the cache after it is fully initialized
     */
    private static SimpleContainer containerInventory;

    /**
     * The storage key to save {@link ContainerPreviewManager#containerInventory} to
     */
    private static String storageKey;

    /**
     * Whether we are currently frozen in the container preview
     */
    @Getter
    private static boolean frozen;

    /**
     * True when we are drawing an {@code ItemStack}'s tooltip while {@link #isFrozen()} is true
     */
    private static boolean drawingFrozenItemTooltip;

    /**
     * Creates and returns a {@code ContainerPreview} object representing the given {@code ItemStack} if it is a backpack
     * @param stack the {@code ItemStack} to create a {@code Backpack} instance from
     * @return a {@code ContainerPreview} object representing {@code stack} if it is a backpack, or {@code null} otherwise
     */
    public static ContainerPreview getFromItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        CustomData extraAttributes = ItemUtils.getExtraAttributes(stack);
        String skyblockID = ItemUtils.getSkyblockItemID(extraAttributes);
        ContainerData containerData = ItemUtils.getContainerData(skyblockID);
        if (extraAttributes != null && containerData != null) {
            int containerSize = containerData.getSize();

            // Parse out a list of items in the container
            final List<ItemStack> items;
            String compressedDataTag = containerData.getCompressedItemStacksTag();
            List<String> dataTags = containerData.getItemStackDataTags();
            CompoundTag nbt = extraAttributes.copyTag();

            if (compressedDataTag != null && nbt.contains(compressedDataTag)) {
                byte[] bytes = nbt.getByteArray(compressedDataTag).orElse(null);
                items = decompressItems(bytes);
            } else if (dataTags != null) {
                items = new ArrayList<>(containerSize);
                Iterator<String> dataTagsIterator = dataTags.iterator();
                for (int itemNumber = 0; itemNumber < containerSize && dataTagsIterator.hasNext(); itemNumber++) {
                    String key = dataTagsIterator.next();
                    if (extraAttributes.contains(key)) {
                        extraAttributes.read(Codec.STRING.fieldOf(key)).result().ifPresent(id ->
                            items.add(ItemUtils.getPersonalCompactorItemStack(id))
                        );
                    } else {
                        ItemStack pane = Blocks.LIME_STAINED_GLASS_PANE.asItem().getDefaultInstance();
                        pane.set(DataComponents.CUSTOM_NAME, Component.literal("§aAuto-Craft Slot #" + (itemNumber + 1)));
                        items.add(pane);
                    }
                }
            } else {
                return null;
            }

            // Get the container color
            BackpackColor color = ItemUtils.getBackpackColor(stack);
            ContainerType type = containerData.getType();
            String name = null;
            if (type != null) {
                switch (containerData.getType()) {
                    case PERSONAL_COMPACTOR:
                    case PERSONAL_DELETOR:
                        break;
                    default:
                        // If type is not null and name is not ignored
                        Component customName = stack.getCustomName();
                        if (customName != null) {
                            name = TextUtils.stripColor(customName.getString());
                        }
                        break;
                }
            }

            boolean active = extraAttributes.read(Codec.BOOL.fieldOf("PERSONAL_DELETOR_ACTIVE")).result().orElse(false);

            return new ContainerPreview(items, name, color, containerData.getNumRows(), containerData.getNumCols(), type, active);
        }
        return null;
    }

    /**
     * Saves {@code containerInventory} to the container inventory cache if it's not {@code null} when a
     * {@link net.minecraft.client.gui.screens.inventory.ContainerScreen} is closed.
     * @see com.fix3dll.skyblockaddons.listeners.ScreenListener#onGuiOpen(Screen)
     */
    public static void onContainerClose() {
        if (containerInventory != null) {
            saveStorageContainerInventory();
            main.getInventoryUtils().setInventoryPageNum(0);
        }
    }

    /**
     * Prepares for saving the container inventory when the container is closed.
     * Called when a {@link net.minecraft.client.gui.screens.inventory.ContainerScreen} is opened.
     * @param containerInventory the container inventory
     */
    public static void onContainerOpen(@NonNull SimpleContainer containerInventory) {
        ContainerPreviewManager.containerInventory = containerInventory;
        storageKey = main.getInventoryUtils().getInventoryKey();
    }

    private static List<ItemStack> decompressItems(byte[] bytes) {
        List<ItemStack> items = null;
        try {
            CompoundTag decompressedData = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
            ListTag list = decompressedData.getList("i").orElse(null);
            if (list == null || list.isEmpty()) {
                throw new Exception("Decompressed container list has no item tags");
            }
            int size = Math.min(list.size(), 54);
            items = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                CompoundTag itemTag = list.getCompoundOrEmpty(i); // 1.21.5
                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                short itemID = itemTag.getShort("id").orElse((short) 0);
                if (itemID == 142) { // Potato Block -> Potato Item
                    itemTag.putShort("id", (short) 392);
                } else if (itemID == 141) { // Carrot Block -> Carrot Item
                    itemTag.putShort("id", (short) 391);
                }
                if (itemTag.isEmpty()) {
                    items.add(ItemStack.EMPTY);
                } else if (Minecraft.getInstance().level != null) {
                    Optional<ItemStack> itemStack = ItemStack.parse(Minecraft.getInstance().level.registryAccess(), itemTag);
                    items.add(itemStack.orElse(ItemStack.EMPTY));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("There was an error decompressing container data.", ex);
        }
        return items;
    }

    public static void drawContainerPreviews(GuiGraphics graphics, AbstractContainerScreen<?> guiContainer, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Feature backpackPreview = Feature.SHOW_BACKPACK_PREVIEW;

        if (currentContainerPreview == null) return;

        int x = currentContainerPreview.getX();
        int y = currentContainerPreview.getY();

        List<ItemStack> items = currentContainerPreview.getItems();
        int length = items.size();
        int rows = currentContainerPreview.getNumRows();
        int cols = currentContainerPreview.getNumCols();

        int screenHeight = guiContainer.height;

        ItemStack tooltipItem = null;

        PoseStack poseStack = graphics.pose();

        if (backpackPreview.get(FeatureSetting.BACKPACK_STYLE) == EnumUtils.BackpackStyle.GUI) {
//            GlStateManager.disableLighting();
            poseStack.pushPose();
            poseStack.translate(0, 0, 300);
            int textColor = 4210752;
            int containerColor = -1;
            if (backpackPreview.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
                BackpackColor color = currentContainerPreview.getBackpackColor();
                ContainerType type = currentContainerPreview.getContainerType();
                if (color != null && color != BackpackColor.DEFAULT) {
                    containerColor = ARGB.colorFromFloat(1.0F, color.getR(), color.getG(), color.getB());
                    textColor = color.getInventoryTextColor();
                } else if (type == ContainerType.PERSONAL_COMPACTOR || type == ContainerType.PERSONAL_DELETOR) {
                    if (currentContainerPreview.isActive()) {
                        containerColor = ARGB.colorFromFloat(1.0F, 0.306F, 0.533F, 0.173F);
                    } else {
                        containerColor = ARGB.colorFromFloat(1.0F, 0.733F, 0.227F, 0.176F);
                    }
                }
            }

            final int textureBorder = 7;
            final int textureTopBorder = 17;
            final int textureItemSquare = 18;

            // Our chest has these properties
            final int topBorder = currentContainerPreview.getName() == null ? textureBorder : textureTopBorder;
            int totalWidth = cols * textureItemSquare + 2 * textureBorder;
            int totalHeight = rows * textureItemSquare + topBorder + textureBorder;
            int squaresEndWidth = totalWidth - textureBorder;
            int squaresEndHeight = totalHeight - textureBorder;

            if (x + totalWidth > guiContainer.width) {
                x -= totalWidth;
            }

            if (y + totalHeight > screenHeight) {
                y = screenHeight - totalHeight;
            }

            // If there is no name, don't render the full top of the chest to make things look cleaner
            if (currentContainerPreview.getName() == null) {
                // Draw top border
                graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x, y, 0, 0, squaresEndWidth, topBorder, 256, 256, containerColor);
                // Draw left-side and all GUI display rows ("squares")
                graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x, y + topBorder, 0, textureTopBorder, squaresEndWidth, squaresEndHeight - topBorder, 256, 256, containerColor);
            } else {
                // Draw the top-left of the container
                graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x, y, 0, 0, squaresEndWidth, squaresEndHeight, 256, 256, containerColor);
            }
            // Draw the top-right of the container
            graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x + squaresEndWidth, y, 169, 0, textureBorder, squaresEndHeight, 256, 256, containerColor);
            // Draw the bottom-left of the container
            graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x, y + squaresEndHeight, 0, 125, squaresEndWidth, textureBorder, 256, 256, containerColor);
            // Draw the bottom-right of the container
            graphics.blit(RenderType::guiTexturedOverlay, CHEST_GUI_TEXTURE, x + squaresEndWidth, y + squaresEndHeight, 169, 125, textureBorder, textureBorder, 256, 256, containerColor);

            if (currentContainerPreview.getName() != null) {
                String name = currentContainerPreview.getName();
                if (main.getUtils().isUsingFSRcontainerPreviewTexture()) {
                    name = ColorCode.GOLD + TextUtils.stripColor(name);
                }
                poseStack.pushPose();
                poseStack.translate(0, 0, 301);
                graphics.drawString(mc.font, name, x + 8, y + 6, textColor, false);
                poseStack.popPose();
            }

            poseStack.popPose();
//            GlStateManager.enableLighting();

//            RenderHelper.enableGUIStandardItemLighting();
//            GlStateManager.enableRescaleNormal();
            int itemStartX = x + textureBorder + 1;
            int itemStartY = y + topBorder + 1;
            for (int i = 0; i < length; i++) {
                ItemStack item = items.get(i);
                if (item != null) {
                    int itemX = itemStartX + ((i % cols) * textureItemSquare);
                    int itemY = itemStartY + ((i / cols) * textureItemSquare);

                    RenderListener.renderItemAndOverlay(graphics, item, null, itemX, itemY, 200);

                    if (frozen && mouseX > itemX && mouseX < itemX + 16 && mouseY > itemY && mouseY < itemY + 16) {
                        tooltipItem = item;
                    }
                }
            }
        } else {
            int totalWidth = (16 * cols) + 3;
            if (x + totalWidth > guiContainer.width) {
                x -= totalWidth;
            }
            int totalHeight = (16 * rows) + 3;
            if (y + totalHeight > screenHeight) {
                y = screenHeight - totalHeight;
            }

//            GlStateManager.disableLighting();
            poseStack.pushPose();
            poseStack.translate(0,0, 300);
            graphics.fill(RenderType.guiOverlay(), x - 3, y - 3, x + totalWidth, y + totalHeight, getRectColor());
            poseStack.popPose();
//            GlStateManager.enableLighting();

//            RenderHelper.enableGUIStandardItemLighting();
//            GlStateManager.enableRescaleNormal();
            for (int i = 0; i < length; i++) {
                ItemStack item = items.get(i);
                if (item != null) {
                    int itemX = x + ((i % cols) * 16);
                    int itemY = y + ((i / cols) * 16);

                    RenderListener.renderItemAndOverlay(graphics, item, null, itemX, itemY, 200);

                    if (frozen && mouseX > itemX && mouseX < itemX+16 && mouseY > itemY && mouseY < itemY+16) {
                        tooltipItem = item;
                    }
                }
            }
        }
        if (tooltipItem != null) {
            // Translate up to fix patcher glitch
            poseStack.pushPose();
            poseStack.translate(0, 0, 302);
            drawingFrozenItemTooltip = true;
            graphics.renderTooltip(mc.font, tooltipItem, mouseX, mouseY);
            drawingFrozenItemTooltip = false;
            poseStack.popPose();
        }
        if (!frozen) {
            currentContainerPreview = null;
        }
//        GlStateManager.enableLighting();
//        GlStateManager.enableDepth();
//        RenderHelper.enableStandardItemLighting();
    }

    /**
     * Create a {@link ContainerPreview} from a backpack {@code ItemStack} in the storage menu and the list of items in that preview
     * @param stack the backpack {@code ItemStack} that's being hovered over
     * @param items the items in the backpack
     * @return the container preview
     */
    public static ContainerPreview getFromStorageBackpack(ItemStack stack, List<ItemStack> items) {
        if (items == null) {
            return null;
        }

        String stackName = stack.getCustomName() == null ? "?" : stack.getCustomName().getString();
        // Get the container color
        BackpackColor color = ItemUtils.getBackpackColor(stack);
        // Relying on item lore here. Once hypixel settles on a standard for backpacks, we should figure out a better way
        String skyblockID = TextUtils.getBackpackIDFromLore(ItemUtils.getItemLore(stack).getFirst());
        ContainerData containerData = ItemUtils.getContainerData(skyblockID);
        int rows = 6, cols = 9;
        ContainerType containerType = null;
        if (containerData != null) {
            // Hybrid system for jumbo backpacks means they get only 5 rows in the container (but old ones that haven't been converted get 6 outside of it)
            rows = Math.min(containerData.getNumRows(), 5);
            cols = containerData.getNumCols();
            containerType = containerData.getType();
        } else if (stackName.toUpperCase(Locale.US).startsWith("ENDER CHEST")) {
            rows = Math.min(5, (int) Math.ceil(items.size() / 9F));
            containerType = ContainerType.BACKPACK;
        }

        return new ContainerPreview(items, stackName, color, rows, cols, containerType);
    }

    /**
     * Called when a key is typed in a {@link AbstractContainerScreen}. Used to control backpack preview freezing.
     * @param keyCode the key code of the key that was typed
     * @see com.fix3dll.skyblockaddons.mixin.hooks.AbstractContainerScreenHook#keyPressed(Slot, int, CallbackInfoReturnable)
     */
    public static void onContainerKeyTyped(int keyCode) {
        if (keyCode == 1 || keyCode == Minecraft.getInstance().options.keyInventory.key.getValue()) {
            frozen = false;
            currentContainerPreview = null;
            cachedContainerPreview = null;
        }
        // Handle the freeze container toggle
        if (cachedContainerPreview != null && keyCode == SkyblockKeyBinding.FREEZE_BACKPACK.getKeyCode()) {
            frozen = !frozen;
            currentContainerPreview = cachedContainerPreview;
        }
    }

    /**
     * Renders the corresponding container preview if the given {@code ItemStack} is a container.
     * If a container preview is rendered, {@code true} is returned to cancel the original tooltip render event.
     * @param itemStack the {@code ItemStack} to render the container preview for
     * @param x the x-coordinate where the item's tooltip is rendered
     * @param y the y-coordinate where the item's tooltip is rendered
     * @return {@code true} if a container preview is rendered, {@code false} otherwise
     */
    public static boolean onRenderTooltip(ItemStack itemStack, int x, int y) {
        // Cancel tooltips while containers are frozen, and we aren't trying to render a tooltip in the backpack
        if (frozen && !drawingFrozenItemTooltip) {
            return true;
        }

        if (Feature.SHOW_BACKPACK_PREVIEW.isDisabled()) {
            return false;
        }

        Feature backpackPreview = Feature.SHOW_BACKPACK_PREVIEW;

        // Don't show if we only want to show while holding shift, and the player isn't holding shift
        if (backpackPreview.isEnabled(FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT) && !Screen.hasShiftDown()) {
            return false;
        }

        // Do not waste resources to process non-UUID items (except Ender Chests cus icons doesn't have UUID)
        UUID newUuid = ItemUtils.getUuid(itemStack);
        String hoverName = itemStack.getHoverName().getString();
        Matcher m = ENDERCHEST_STORAGE_PATTERN.matcher(hoverName);
        if (newUuid == null && !m.find()) {
            return false;
        }

        if (cachedBackpackUuid == null || !cachedBackpackUuid.equals(newUuid)) {
            cachedBackpackUuid = newUuid;
            cachedContainerPreview = null;

            // Check for cached storage previews
            if (main.getInventoryUtils().getInventoryType() == InventoryType.STORAGE) {
                String storageKey = null;

                if (m.groupCount() != 0) {
                    int enderChestPage = Integer.parseInt(m.group("page"));
                    storageKey = InventoryType.ENDER_CHEST.getInventoryName() + enderChestPage;
                } else if ((m = BACKPACK_STORAGE_PATTERN.matcher(hoverName)).matches()) {
                    int pageNum = Integer.parseInt(m.group("slot"));
                    storageKey = InventoryType.STORAGE_BACKPACK.getInventoryName() + pageNum;
                }

                if (storageKey != null) {
                    Map<String, CompressedStorage> cache = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getStorageCache();
                    if (cache.get(storageKey) != null) {
                        byte[] bytes = cache.get(storageKey).getStorage();
                        List<ItemStack> items = decompressItems(bytes);
                        // Clip out the top
                        items = items.subList(9, items.size());
                        cachedContainerPreview = getFromStorageBackpack(itemStack, items);
                    }
                }
            }
            // Check for normal previews
            if (cachedContainerPreview == null) {
                // Check the sub-feature conditions
                ContainerData containerData = ItemUtils.getContainerData(ItemUtils.getSkyblockItemID(itemStack));

                if (containerData == null ||
                        (containerData.isCakeBag() && backpackPreview.isDisabled(FeatureSetting.CAKE_BAG_PREVIEW)) ||
                        ((containerData.isBuildersRuler() || containerData.isBuildersWand())
                                && backpackPreview.isDisabled(FeatureSetting.BUILDERS_TOOL_PREVIEW)) ||
                        ((containerData.isPersonalCompactor() || containerData.isPersonalDeletor())
                                && backpackPreview.isDisabled(FeatureSetting.PERSONAL_COMPACTOR_PREVIEW)) ||
                        ((containerData.isBasketOfSeeds() || containerData.isNetherWartPouch())
                                && backpackPreview.isDisabled(FeatureSetting.FARMING_TOOLS_PREVIEW))
                ) {
                    return false;
                }

                //TODO: Probably some optimizations here we can do. Can we check chest equivalence?
                // Avoid showing backpack preview in auction stuff.
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof ContainerScreen cScreen && cScreen.getMenu() instanceof ChestMenu chestMenu) {
                    Container chestInventory = chestMenu.getContainer(); // lowerChestInventory
                    String chestName = cScreen.getTitle().getString();
                    if (!chestName.isEmpty()) {
                        if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                            // Make sure this backpack is in the auction house and not just in your inventory before cancelling.
                            for (int slotNumber = 0; slotNumber < chestInventory.getContainerSize(); slotNumber++) {
                                if (chestInventory.getItem(slotNumber) == itemStack) {
                                    return false;
                                }
                            }
                        }
                    }
                }

                cachedContainerPreview = ContainerPreviewManager.getFromItem(itemStack);
            }
        }

        if (cachedContainerPreview != null) {
            cachedContainerPreview.setX(x);
            cachedContainerPreview.setY(y);

            if (!frozen) {
                currentContainerPreview = cachedContainerPreview;
            }
            return true;
        }

        return frozen;
    }

    /**
     * Compresses the contents of the inventory
     * @param inventory the inventory to be compressed
     * @return a nbt byte array of the compressed contents of the backpack
     */
    public static ByteArrayTag getCompressedInventoryContents(Container inventory) {
        if (inventory == null) {
            return null;
        }
        ItemStack[] list = new ItemStack[inventory.getContainerSize()];
        for (int slotNumber = 0; slotNumber < inventory.getContainerSize(); slotNumber++) {
            list[slotNumber] = inventory.getItem(slotNumber);
        }
        return ItemUtils.getCompressedNBT(list);
    }

    /**
     * Saves the currently opened menu inventory to the backpack cache.
     * Triggers {@link com.fix3dll.skyblockaddons.config.PersistentValuesManager#saveValues()} if the inventory has
     * changed from the cached version.
     * @param inventory the inventory to save the contents of
     * @param storageKey the key in which to store the data
     * @throws NullPointerException if {@code inventory} or {@code storageKey} are {@code null}
     */
    public static void saveStorageContainerInventory(SimpleContainer inventory, String storageKey) {
        if (inventory == null) {
            throw new NullPointerException("Cannot save contents of a null inventory.");
        } else if (storageKey == null) {
            throw new NullPointerException("Storage key is required to save the container's inventory.");
        }

        if (!storageKey.equals(ContainerPreviewManager.storageKey)) {
            if (containerInventory != null) {
                saveStorageContainerInventory();
            }

            ContainerPreviewManager.storageKey = storageKey;
        } else {
            // Get the cached storage containers
            Map<String, CompressedStorage> cache = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getStorageCache();
            // Get the cached container stored at this key
            CompressedStorage cachedContainer = cache.get(storageKey);
            byte[] previousCache = cachedContainer == null ? null : cachedContainer.getStorage();

            // Compute the compressed inventory of the current open inventory
            byte[] inventoryContents = getCompressedInventoryContents(inventory).getAsByteArray();

            // Check if the cache is dirty
            boolean dirty = previousCache == null || !Arrays.equals(previousCache, inventoryContents);

            if (dirty) {
                if (cachedContainer == null) {
                    cache.put(storageKey, new CompressedStorage(inventoryContents));
                    LOGGER.info("Cached new container {}.", storageKey);
                } else {
                    cachedContainer.setStorage(inventoryContents);
                    LOGGER.info("Refreshed cache for container {}.", storageKey);
                }

                SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
            }

            resetCurrentContainer();
        }
    }

    /**
     * Saves the currently opened menu inventory to the backpack cache.
     * Triggers {@link com.fix3dll.skyblockaddons.config.PersistentValuesManager#saveValues()} if the inventory has
     * changed from the cached version.
     * @throws NullPointerException if {@link ContainerPreviewManager#containerInventory} or
     * {@link ContainerPreviewManager#storageKey} are {@code null}
     */
    public static void saveStorageContainerInventory() {
        saveStorageContainerInventory(containerInventory, storageKey);
    }

    /**
     * Resets the current container being cached by the manager.
     */
    private static void resetCurrentContainer() {
        containerInventory = null;
        storageKey = null;
    }

    private static int getRectColor() {
        int rectColor = ColorCode.DARK_GRAY.getColor(250);

        if (Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            ContainerType type = currentContainerPreview.getContainerType();
            if (type == ContainerType.PERSONAL_COMPACTOR || type == ContainerType.PERSONAL_DELETOR) {
                if (currentContainerPreview.isActive()) {
                    rectColor = ColorCode.DARK_GREEN.getColor(250);
                } else {
                    rectColor = ColorCode.DARK_RED.getColor(250);
                }
            }
        }
        return rectColor;
    }
}