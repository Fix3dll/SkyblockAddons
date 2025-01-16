package codes.biscuit.skyblockaddons.utils.gson;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValuesManager.ConfigValues;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

/**
 * Customized deserialization of {@link ConfigValues} {@link ConfigValues} It parses each Feature in a tolerant manner.
 * If any Feature object is read incorrectly, instead of failing completely, it marks that Feature to take its default
 * value and continues reading the remaining properties.
 */
public class ConfigValuesAdapter implements JsonDeserializer<ConfigValues> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @Override
    public ConfigValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ConfigValues configValues = new ConfigValues();

        if (jsonObject.has("configVersion")) {
            configValues.setConfigVersion(jsonObject.get("configVersion").getAsInt());
        }
        if (jsonObject.has("features")) {
            JsonObject featuresObject = jsonObject.getAsJsonObject("features");
            EnumMap<Feature, FeatureData<?>> features = new EnumMap<>(Feature.class);

            for (Map.Entry<String, JsonElement> entry : featuresObject.entrySet()) {
                try {
                    features.put(Feature.valueOf(entry.getKey()), context.deserialize(entry.getValue(), FeatureData.class));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Could not parse feature {}. If it is a deprecated feature, ignore it.", entry.getKey());
                    LOGGER.catching(e);
                } catch (JsonParseException e) {
                    LOGGER.error("Could not parse feature {}. FeatureData will be replaced with defaults.", entry.getKey());
                    LOGGER.catching(e);
                    features.put(Feature.valueOf(entry.getKey()), null);
                }
            }

            configValues.setFeatures(features);
        }

        return configValues;
    }
}