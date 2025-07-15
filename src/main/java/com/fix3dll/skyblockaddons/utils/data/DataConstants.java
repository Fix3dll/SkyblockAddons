package com.fix3dll.skyblockaddons.utils.data;

class DataConstants {

    static final String BRANCH = "1.21.5";
    static final String CDN_BASE_URL = String.format(
            "https://cdn.jsdelivr.net/gh/Fix3dll/SkyblockAddons-Data@%s/", BRANCH
    );
    static final String FALLBACK_CDN_BASE_URL = String.format(
            "https://fastly.jsdelivr.net/gh/Fix3dll/SkyblockAddons-Data@%s/", BRANCH
    );

}