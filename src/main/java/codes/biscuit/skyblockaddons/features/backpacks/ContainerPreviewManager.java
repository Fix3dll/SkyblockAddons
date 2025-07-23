package codes.biscuit.skyblockaddons.features.backpacks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.ContainerData;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.ContainerData.ContainerType;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
public class ContainerPreviewManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("skyblockaddons", "containerPreview.png");
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
    private static InventoryBasic containerInventory;

    /**
     * The storage key to save {@link ContainerPreviewManager#containerInventory} to
     */
    private static String storageKey;

    /**
     * Whether we are currently frozen in the container preview
     */
    @Getter private static boolean frozen;

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

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(stack);
        String skyblockID = ItemUtils.getSkyblockItemID(extraAttributes);
        ContainerData containerData = ItemUtils.getContainerData(skyblockID);
        if (extraAttributes != null && containerData != null) {
            int containerSize = containerData.getSize();

            // Parse out a list of items in the container
            List<ItemStack> items = null;
            String compressedDataTag = containerData.getCompressedItemStacksTag();
            List<String> dataTags = containerData.getItemStackDataTags();

            if (compressedDataTag != null && extraAttributes.hasKey(compressedDataTag, Constants.NBT.TAG_BYTE_ARRAY)) {
                byte[] bytes = extraAttributes.getByteArray(compressedDataTag);
                items = decompressItems(bytes);
            } else if (dataTags != null) {
                items = new ArrayList<>(containerSize);
                Iterator<String> dataTagsIterator = dataTags.iterator();
                for (int itemNumber = 0; itemNumber < containerSize && dataTagsIterator.hasNext(); itemNumber++) {
                    String key = dataTagsIterator.next();
                    if (extraAttributes.hasKey(key)) {
                        items.add(ItemUtils.getPersonalCompactorItemStack(extraAttributes.getString(key)));
                    } else {
                        items.add(
                                new ItemStack(Blocks.stained_glass_pane, 1, 5)
                                        .setStackDisplayName("§aAuto-Craft Slot #" + (itemNumber + 1))
                        );
                    }
                }
            }
            if (items == null) {
                return null;
            }

            // Get the container color
            BackpackColor color = ItemUtils.getBackpackColor(stack);
            ContainerType type = containerData.getType();
            String name = null;

            if (type != null) {
                switch (type) {
                    case PERSONAL_COMPACTOR:
                    case PERSONAL_DELETOR:
                        break;
                    default:
                        // If type is not null and name is not ignored
                        name = TextUtils.stripColor(stack.getDisplayName());
                        break;
                }
            }

            boolean active = false;
            if (extraAttributes.hasKey("PERSONAL_DELETOR_ACTIVE")) {
                active = extraAttributes.getBoolean("PERSONAL_DELETOR_ACTIVE");
            }

            return new ContainerPreview(items, name, color, containerData.getNumRows(), containerData.getNumCols(), containerData.getType(), active);
        }
        return null;
    }

    /**
     * Saves {@code containerInventory} to the container inventory cache if it's not {@code null} when a
     * {@link net.minecraft.client.gui.inventory.GuiChest} is closed.
     * @see codes.biscuit.skyblockaddons.listeners.GuiScreenListener#onGuiOpen(GuiOpenEvent)
     */
    public static void onContainerClose() {
        if (containerInventory != null) {
            saveStorageContainerInventory();
            main.getInventoryUtils().setInventoryPageNum(0);
        }
    }

    /**
     * Prepares for saving the container inventory when the container is closed.
     * Called when a {@link net.minecraft.client.gui.inventory.GuiChest} is opened.
     * @param containerInventory the container inventory
     */
    public static void onContainerOpen(@NonNull InventoryBasic containerInventory) {
        ContainerPreviewManager.containerInventory = containerInventory;
        storageKey = main.getInventoryUtils().getInventoryKey();
    }

    private static List<ItemStack> decompressItems(byte[] bytes) {
        List<ItemStack> items = null;
        try {
            NBTTagCompound decompressedData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            NBTTagList list = decompressedData.getTagList("i", Constants.NBT.TAG_COMPOUND);
            if (list.hasNoTags()) {
                throw new Exception("Decompressed container list has no item tags");
            }
            int size = Math.min(list.tagCount(), 54);
            items = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                NBTTagCompound item = list.getCompoundTagAt(i);
                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                short itemID = item.getShort("id");
                if (itemID == 142) { // Potato Block -> Potato Item
                    item.setShort("id", (short) 392);
                } else if (itemID == 141) { // Carrot Block -> Carrot Item
                    item.setShort("id", (short) 391);
                }
                items.add(ItemStack.loadItemStackFromNBT(item));
            }
        } catch (Exception ex) {
            LOGGER.error("There was an error decompressing container data.", ex);
        }
        return items;
    }

    public static void drawContainerPreviews(GuiContainer guiContainer, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        Feature backpackPreview = Feature.SHOW_BACKPACK_PREVIEW;

        if (currentContainerPreview == null) return;

        int x = currentContainerPreview.getX();
        int y = currentContainerPreview.getY();

        List<ItemStack> items = currentContainerPreview.getItems();
        int length = items.size();
        int rows = currentContainerPreview.getNumRows();
        int cols = currentContainerPreview.getNumCols();

        int screenHeight = guiContainer.height;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        ItemStack tooltipItem = null;

        if (backpackPreview.get(FeatureSetting.BACKPACK_STYLE) == EnumUtils.BackpackStyle.GUI) {
            mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0,0,300);
            int textColor = 4210752;
            if (backpackPreview.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
                BackpackColor color = currentContainerPreview.getBackpackColor();
                ContainerType type = currentContainerPreview.getContainerType();
                if (color != null && color != BackpackColor.DEFAULT) {
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1.0F);
                    textColor = color.getInventoryTextColor();
                } else if (type == ContainerType.PERSONAL_COMPACTOR || type == ContainerType.PERSONAL_DELETOR) {
                    if (currentContainerPreview.isActive()) {
                        GlStateManager.color(0.306F, 0.533F, 0.173F, 1.0F);
                    } else {
                        GlStateManager.color(0.733F, 0.227F, 0.176F, 1.0F);
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
                guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, topBorder);
                // Draw left-side and all GUI display rows ("squares")
                guiContainer.drawTexturedModalRect(x, y + topBorder, 0, textureTopBorder, squaresEndWidth, squaresEndHeight - topBorder);
            } else {
                // Draw the top-left of the container
                guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, squaresEndHeight);
            }
            // Draw the top-right of the container
            guiContainer.drawTexturedModalRect(x + squaresEndWidth, y, 169, 0, textureBorder, squaresEndHeight);
            // Draw the bottom-left of the container
            guiContainer.drawTexturedModalRect(x, y + squaresEndHeight, 0, 125, squaresEndWidth, textureBorder);
            // Draw the bottom-right of the container
            guiContainer.drawTexturedModalRect(x + squaresEndWidth, y + squaresEndHeight, 169, 125, textureBorder, textureBorder);

            if (currentContainerPreview.getName() != null) {
                String name = currentContainerPreview.getName();
                if (main.getUtils().isUsingFSRcontainerPreviewTexture()) {
                    name = ColorCode.GOLD + TextUtils.stripColor(name);
                }
                mc.fontRendererObj.drawString(name, x + 8, y + 6, textColor);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            int itemStartX = x + textureBorder + 1;
            int itemStartY = y + topBorder + 1;
            for (int i = 0; i < length; i++) {
                ItemStack item = items.get(i);
                if (item != null) {
                    int itemX = itemStartX + ((i % cols) * textureItemSquare);
                    int itemY = itemStartY + ((i / cols) * textureItemSquare);

                    RenderItem renderItem = mc.getRenderItem();
                    guiContainer.zLevel = 200;
                    renderItem.zLevel = 200;
                    renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                    renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                    guiContainer.zLevel = 0;
                    renderItem.zLevel = 0;

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

            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0,0, 300);
            Gui.drawRect(x - 3, y - 3, x + totalWidth, y + totalHeight, getRectColor());
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            for (int i = 0; i < length; i++) {
                ItemStack item = items.get(i);
                if (item != null) {
                    int itemX = x + ((i % cols) * 16);
                    int itemY = y + ((i / cols) * 16);

                    RenderItem renderItem = mc.getRenderItem();
                    guiContainer.zLevel = 200;
                    renderItem.zLevel = 200;
                    renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                    renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                    guiContainer.zLevel = 0;
                    renderItem.zLevel = 0;

                    if (frozen && mouseX > itemX && mouseX < itemX+16 && mouseY > itemY && mouseY < itemY+16) {
                        tooltipItem = item;
                    }
                }
            }
        }
        if (tooltipItem != null) {
            // Translate up to fix patcher glitch
            GlStateManager.pushMatrix();
            GlStateManager.translate(0,0, 302);
            drawingFrozenItemTooltip = true;
            DrawUtils.drawHoveringText(
                    tooltipItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips),
                    mouseX,
                    mouseY,
                    guiContainer.width,
                    guiContainer.height,
                    -1
            );
            drawingFrozenItemTooltip = false;
            GlStateManager.popMatrix();
        }
        if (!frozen) {
            currentContainerPreview = null;
        }
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
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

        // Get the container color
        BackpackColor color = ItemUtils.getBackpackColor(stack);
        // Relying on item lore here. Once hypixel settles on a standard for backpacks, we should figure out a better way
        String skyblockID = TextUtils.getBackpackIDFromLore(ItemUtils.getItemLore(stack).get(0));
        ContainerData containerData = ItemUtils.getContainerData(skyblockID);
        int rows = 6, cols = 9;
        ContainerType containerType = null;
        if (containerData != null) {
            // Hybrid system for jumbo backpacks means they get only 5 rows in the container (but old ones that haven't been converted get 6 outside of it)
            rows = Math.min(containerData.getNumRows(), 5);
            cols = containerData.getNumCols();
            containerType = containerData.getType();
        } else if (TextUtils.stripColor(stack.getDisplayName()).toUpperCase().startsWith("ENDER CHEST")) {
            rows = Math.min(5, (int) Math.ceil(items.size() / 9F));
            containerType = ContainerType.BACKPACK;
        }

        return new ContainerPreview(items, TextUtils.stripColor(stack.getDisplayName()), color, rows, cols, containerType);
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

        Feature backpackPreview = Feature.SHOW_BACKPACK_PREVIEW;
        if (backpackPreview.isDisabled()) {
            return false;
        }

        // Don't show if we only want to show while holding shift, and the player isn't holding shift
        if (backpackPreview.isEnabled(FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
            return false;
        }

        // Do not waste resources to process non-UUID items (except Ender Chests cus icons doesn't have UUID)
        UUID newUuid = ItemUtils.getUuid(itemStack);
        String strippedDisplayName = TextUtils.stripColor(itemStack.getDisplayName());
        Matcher m = ENDERCHEST_STORAGE_PATTERN.matcher(strippedDisplayName);
        boolean enderChestMatched = false;
        if (newUuid == null && !(enderChestMatched = m.find())) {
            return false;
        }

        if (cachedBackpackUuid == null || !cachedBackpackUuid.equals(newUuid)) {
            cachedBackpackUuid = newUuid;
            cachedContainerPreview = null;

            // Check for cached storage previews
            if (main.getInventoryUtils().getInventoryType() == InventoryType.STORAGE) {
                String storageKey = null;

                if (enderChestMatched) {
                    int enderChestPage = Integer.parseInt(m.group("page"));
                    storageKey = InventoryType.ENDER_CHEST.getInventoryName() + enderChestPage;
                } else if ((m = BACKPACK_STORAGE_PATTERN.matcher(strippedDisplayName)).matches()) {
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

                // TODO: Does checking menu item handle the baker inventory thing?
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
                Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
                if (playerContainer instanceof ContainerChest) {
                    IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
                    if (chestInventory.hasCustomName()) {
                        String chestName = chestInventory.getDisplayName().getUnformattedText();
                        if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                            // Make sure this backpack is in the auction house and not just in your inventory before cancelling.
                            for (int slotNumber = 0; slotNumber < chestInventory.getSizeInventory(); slotNumber++) {
                                if (chestInventory.getStackInSlot(slotNumber) == itemStack) {
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
     * Called when a key is typed in a {@link GuiContainer}. Used to control backpack preview freezing.
     * @param keyCode the key code of the key that was typed
     */
    public static void onContainerKeyTyped(int keyCode) {
        if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
            frozen = false;
            currentContainerPreview = null;
            cachedContainerPreview = null;
        }

        // Handle the freeze container toggle
        if (cachedContainerPreview != null && SkyblockKeyBinding.FREEZE_BACKPACK.isKeyDown()) {
            frozen = !frozen;
            currentContainerPreview = cachedContainerPreview;
        }
    }

    /**
     * Compresses the contents of the inventory
     * @param inventory the inventory to be compressed
     * @return a nbt byte array of the compressed contents of the backpack
     */
    public static NBTTagByteArray getCompressedInventoryContents(IInventory inventory) {
        if (inventory == null) {
            return null;
        }
        ItemStack[] list = new ItemStack[inventory.getSizeInventory()];
        for (int slotNumber = 0; slotNumber < inventory.getSizeInventory(); slotNumber++) {
            list[slotNumber] = inventory.getStackInSlot(slotNumber);
        }
        return ItemUtils.getCompressedNBT(list);
    }

    /**
     * Saves the currently opened menu inventory to the backpack cache.
     * Triggers {@link PersistentValuesManager#saveValues()} if the inventory has changed from the cached version.
     * @param inventory the inventory to save the contents of
     * @param storageKey the key in which to store the data
     * @throws NullPointerException if {@code inventory} or {@code storageKey} are {@code null}
     */
    public static void saveStorageContainerInventory(InventoryBasic inventory, String storageKey) {
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
            byte[] inventoryContents = getCompressedInventoryContents(inventory).getByteArray();

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
     * Triggers {@link PersistentValuesManager#saveValues()} if the inventory has changed from the cached version.
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