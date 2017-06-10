package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A section of a building. Can be either a complete floor or part of a floor.
 */
public class BuildingPart implements IAsset {

    private String name;

    // Data per height level
    private String[] slices;

    // Dimension (should be less then 16x16)
    private int xSize;
    private int zSize;

    private Map<String, Object> metadata = new HashMap<>();

    public BuildingPart(JsonObject object) {
        readFromJSon(object);
    }

    public BuildingPart(String name, int xSize, int zSize, String[] slices) {
        this.name = name;
        this.slices = slices;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    public Integer getMetaInteger(String key) {
        return (Integer) metadata.get(key);
    }
    public Boolean getMetaBoolean(String key) {
        return (Boolean) metadata.get(key);
    }
    public Float getMetaFloat(String key) {
        return (Float) metadata.get(key);
    }
    public String getMetaString(String key) {
        return (String) metadata.get(key);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        xSize = object.get("xsize").getAsInt();
        zSize = object.get("zsize").getAsInt();
        JsonArray sliceArray = object.get("slices").getAsJsonArray();
        slices = new String[sliceArray.size()];
        int i = 0;
        for (JsonElement element : sliceArray) {
            JsonArray a = element.getAsJsonArray();
            String slice = "";
            for (JsonElement el : a) {
                slice += el.getAsString();
            }
            slices[i++] = slice;
        }
        if (object.has("meta")) {
            JsonArray metaArray = object.get("meta").getAsJsonArray();
            for (JsonElement element : metaArray) {
                JsonObject o = element.getAsJsonObject();
                String key = o.get("key").getAsString();
                if (o.has("integer")) {
                    metadata.put(key, o.get("integer").getAsInt());
                } else if (o.has("float")) {
                    metadata.put(key, o.get("float").getAsFloat());
                } else if (o.has("boolean")) {
                    metadata.put(key, o.get("boolean").getAsBoolean());
                } else if (o.has("string")) {
                    metadata.put(key, o.get("string").getAsString());
                }
            }
        }
    }

    @Override
    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("part"));
        object.add("name", new JsonPrimitive(name));
        object.add("xsize", new JsonPrimitive(xSize));
        object.add("zsize", new JsonPrimitive(zSize));
        JsonArray sliceArray = new JsonArray();
        for (String slice : slices) {
            JsonArray a = new JsonArray();
            while (!slice.isEmpty()) {
                String left = StringUtils.left(slice, xSize);
                a.add(new JsonPrimitive(left));
                slice = slice.substring(left.length());
            }
            sliceArray.add(a);
        }
        object.add("slices", sliceArray);

        JsonArray metaArray = new JsonArray();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("key", new JsonPrimitive(entry.getKey()));
            Object v = entry.getValue();
            if (v instanceof Integer) {
                o.add("integer", new JsonPrimitive((Integer) v));
            } else if (v instanceof Float) {
                o.add("float", new JsonPrimitive((Float) v));
            } else if (v instanceof Boolean) {
                o.add("boolean", new JsonPrimitive((Boolean) v));
            } else if (v instanceof String) {
                o.add("string", new JsonPrimitive((String) v));
            }
            metaArray.add(o);
        }
        object.add("meta", metaArray);

        return object;
    }

    public int getSliceCount() {
        return slices.length;
    }

    public String getSlice(int i) {
        return slices[i];
    }

    public String[] getSlices() {
        return slices;
    }

    public int getXSize() {
        return xSize;
    }

    public int getZSize() {
        return zSize;
    }

    public IBlockState get(BuildingInfo info, int x, int y, int z) {
        return info.getCompiledPalette().get(slices[y].charAt(z * xSize + x), info);
    }

    public Character getC(int x, int y, int z) {
        return slices[y].charAt(z * xSize + x);
    }
}