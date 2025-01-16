package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Brian Graham (CraftedFury)
 */
public enum ColorCode implements RegistrableEnum {
	BLACK('0', 0xFF000000),
	DARK_BLUE('1', 0xFF0000AA),
	DARK_GREEN('2', 0xFF00AA00),
	DARK_AQUA('3', 0xFF00AAAA),
	DARK_RED('4', 0xFFAA0000),
	DARK_PURPLE('5', 0xFFAA00AA),
	GOLD('6', 0xFFFFAA00),
	GRAY('7', 0xFFAAAAAA),
	DARK_GRAY('8', 0xFF555555),
	BLUE('9', 0xFF5555FF),
	GREEN('a', 0xFF55FF55),
	AQUA('b', 0xFF55FFFF),
	RED('c', 0xFFFF5555),
	LIGHT_PURPLE('d', 0xFFFF55FF),
	YELLOW('e', 0xFFFFFF55),
	WHITE('f', 0xFFFFFFFF),
	MAGIC('k', true, "obfuscated"),
	BOLD('l', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true, "underlined"),
	ITALIC('o', true),
	RESET('r'),
	CHROMA('z', 0xFFFFFFFE);

	public static final char COLOR_CHAR = '§';
	@Getter private final char code;
	private final boolean isFormat;
	@Getter private final String jsonName;
	private final String toString;
	@Getter private final int color;

	ColorCode(char code) {
		this(code, -1);
	}

	ColorCode(char code, int rgb) {
		this(code, false, rgb);
	}

	ColorCode(char code, boolean isFormat) {
		this(code, isFormat, -1);
	}

	ColorCode(char code, boolean isFormat, int rgb) {
		this(code, isFormat, null, rgb);
	}

	ColorCode(char code, boolean isFormat, String jsonName) {
		this(code, isFormat, jsonName, -1);
	}

	ColorCode(char code, boolean isFormat, String jsonName, int rgb) {
		this.code = code;
		this.isFormat = isFormat;
		this.jsonName = StringUtils.isEmpty(jsonName) ? this.name().toLowerCase() : jsonName;
		this.toString = new String(new char[] { COLOR_CHAR, code });
		this.color = rgb;
	}

	/**
	 * Get the color represented by the specified code.
	 *
	 * @param code The code to search for.
	 * @return The mapped color, or null if non exists.
	 */
	public static ColorCode getByChar(char code) {
		for (ColorCode color : values()) {
			if (color.code == code)
				return color;
		}

		return null;
	}

	public static ColorCode getByARGB(int argb) {
		for (ColorCode color : values()) {
			if (color.color == argb)
				return color;
		}

		return null;
	}

	public int getColor(int alpha) {
		return ColorUtils.setColorAlpha(color, alpha);
	}

    public boolean isColor() {
		return !this.isFormat && this != RESET;
	}

	@Override
	public String toString() {
		return this.toString;
	}
}