package com.fix3dll.skyblockaddons.mixin.accessors;

import net.minecraft.client.KeyMapping;

public interface GameOptionsAccessor {

    void sba$updateAllKeys(KeyMapping[] updatedAllKeys);

    KeyMapping[] sba$getAllKeys();

}
