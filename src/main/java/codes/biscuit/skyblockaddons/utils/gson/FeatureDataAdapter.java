package codes.biscuit.skyblockaddons.utils.gson;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.FeatureData;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumRegistry;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AnchorPoint;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * Serializes/deserializes FeatureData, which contains data specific to each feature.
 * @see FeatureData
 */
public class FeatureDataAdapter<T> implements JsonDeserializer<FeatureData<T>>, JsonSerializer<FeatureData<T>> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final Float DEFAULT_SCALE = 1.0F;
    private static final int DEFAULT_COLOR = ColorCode.RED.getColor();

    @Override
    public FeatureData<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        FeatureData<T> featureData = new FeatureData<>(null);

        if (jsonObject.has("value")) {
            featureData.setValue(this.deserializedValue(jsonObject.get("value"), context));
        } else {
            throw new JsonParseException("FeatureData value cannot be null!");
        }
        if (jsonObject.has("anchorPoint")) {
            featureData.setAnchorPoint(context.deserialize(jsonObject.get("anchorPoint"), AnchorPoint.class));
        }
        if (jsonObject.has("coords")) {
            JsonArray coords = jsonObject.get("coords").getAsJsonArray();
            featureData.setCoords(new Pair<>(coords.get(0).getAsFloat(), coords.get(1).getAsFloat()));
        }
        if (jsonObject.has("barSizes")) {
            JsonArray barSizes = jsonObject.get("barSizes").getAsJsonArray();
            featureData.setBarSizes(new Pair<>(barSizes.get(0).getAsFloat(), barSizes.get(1).getAsFloat()));
        }
        if (jsonObject.has("guiScale")) {
            featureData.setGuiScale(jsonObject.get("guiScale").getAsFloat());
        }
        if (jsonObject.has("color")) {
            featureData.setColor(jsonObject.get("color").getAsInt());
        }
        if (jsonObject.has("chroma")) {
            featureData.setChroma(jsonObject.get("chroma").getAsBoolean());
        }

        // Deserialize settings map
        if (jsonObject.has("settings")) {
            JsonObject settingsJson = jsonObject.getAsJsonObject("settings");
            TreeMap<FeatureSetting, Object> settings = deserializeSettings(settingsJson);

            if (!settings.isEmpty()) {
                featureData.setSettings(settings);
            }
        }

        return featureData;
    }

    @Override
    public JsonElement serialize(FeatureData<T> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        Object value = src.getValue();
        if (src.getValue() != null) {
            this.serializeValue(value, jsonObject, context);
        } else {
            throw new JsonParseException("FeatureData value cannot be null!");
        }
        if (src.getAnchorPoint() != null) {
            jsonObject.add("anchorPoint", context.serialize(src.getAnchorPoint()));
        }
        if (src.getCoords() != null) {
            JsonArray coords = new JsonArray();
            coords.add(new JsonPrimitive(src.getCoords().getLeft()));
            coords.add(new JsonPrimitive(src.getCoords().getRight()));
            jsonObject.add("coords", coords);
        }
        if (!DEFAULT_SCALE.equals(src.getBarSizes().getLeft()) || !DEFAULT_SCALE.equals(src.getBarSizes().getRight())) {
            JsonArray barSizes = new JsonArray();
            barSizes.add(new JsonPrimitive(src.getBarSizes().getLeft()));
            barSizes.add(new JsonPrimitive(src.getBarSizes().getRight()));
            jsonObject.add("barSizes", barSizes);
        }
        if (src.getGuiScale() != DEFAULT_SCALE) {
            jsonObject.addProperty("guiScale", src.getGuiScale());
        }
        if (src.getColor() != DEFAULT_COLOR || (src.getGuiData() != null && src.getGuiData().getDefaultColor() != null)) {
            jsonObject.addProperty("color", src.getColor());
        }
        if (src.isChroma()) {
            jsonObject.addProperty("chroma", true);
        }

        // Serialize settings map
        TreeMap<FeatureSetting, Object> settings = src.getSettings();
        if (settings != null && !settings.isEmpty()) {
            JsonObject settingsJson = new JsonObject();
            settings.forEach((featureSetting, settingValue) ->
                this.serializeValue(featureSetting.name(), settingValue, settingsJson, context)
            );
            jsonObject.add("settings", settingsJson);
        }

        return jsonObject;
    }

    private void serializeValue(Object value, JsonObject jsonObject, JsonSerializationContext context) {
        serializeValue("value", value, jsonObject, context);
    }

    private void serializeValue(String propertyName, Object value, JsonObject jsonObject, JsonSerializationContext context) {
        if (value instanceof Boolean) {
            jsonObject.addProperty(propertyName, (boolean) value);
        } else if (value instanceof Number) {
            jsonObject.addProperty(propertyName, (Number) value);
        } else if (value instanceof RegistrableEnum) {
            jsonObject.add(propertyName, context.serialize(value));
        } else if (value instanceof String) {
            jsonObject.addProperty(propertyName, (String) value);
        } else {
            throw new JsonParseException("Invalid FeatureData value type: " + value.getClass());
        }
    }

    private Object deserializedValue(JsonElement value, JsonDeserializationContext context) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();

        if (primitive.isBoolean()) {
            return value.getAsBoolean();
        } else if (primitive.isNumber()) {
            return value.getAsNumber();
        } else if (primitive.isString()) {
            Class<?> classType = EnumRegistry.getEnumClass(value.getAsString());
            if (classType != null) {
                return context.<RegistrableEnum>deserialize(value, classType);
            } else {
                return value.getAsString();
            }
        } else {
            throw new JsonParseException("Invalid FeatureData value type: " + primitive.getAsString());
        }
    }

    private TreeMap<FeatureSetting, Object> deserializeSettings(JsonObject settingsJson) {
        TreeMap<FeatureSetting, Object> settings = new TreeMap<>();

        for (Map.Entry<String, JsonElement> entry : settingsJson.entrySet()) {
            FeatureSetting featureSetting;
            try {
                featureSetting =  FeatureSetting.valueOf(entry.getKey());
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid FeatureSetting: {}", entry.getKey());
                continue;
            }

            JsonElement entryValue = entry.getValue();
            if (!entryValue.isJsonPrimitive()) continue;
            JsonPrimitive primitive = entryValue.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                settings.put(featureSetting, primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                settings.put(featureSetting, primitive.getAsNumber());
            } else if (primitive.isString()) {
                try {
                    RegistrableEnum enumKey = EnumRegistry.getEnumValue(entry.getKey(), primitive.getAsString());
                    settings.put(featureSetting, enumKey);
                } catch (IllegalArgumentException e) {
                    settings.put(featureSetting, primitive.getAsString());
                }
            }
        }

        return settings;
    }
}