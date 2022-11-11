package fishcute.celestial.sky;

import com.google.gson.JsonObject;
import fishcute.celestial.util.ColorEntry;
import fishcute.celestial.util.Util;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.awt.*;
import java.util.ArrayList;

public class CelestialObject {
    public final String scale;
    public final CelestialObjectType type;
    public String posX;
    public String posY;
    public String posZ;
    public final String distance;
    public String degreesX;
    public String degreesY;
    public String degreesZ;
    public final String baseDegreesX;
    public final String baseDegreesY;
    public final String baseDegreesZ;

    public double populateDegreesX;
    public double populateDegreesY;
    public double populateDegreesZ;
    public double populateScaleAdd;
    public double populateDistanceAdd;
    public double populatePosX;
    public double populatePosY;
    public double populatePosZ;

    public ColorEntry solidColor;

    public ResourceLocation texture;

    public SkyBoxObjectProperties skyBoxProperties;

    public final CelestialObjectProperties celestialObjectProperties;

    public final ArrayList<Util.VertexPoint> vertexList;

    public CelestialObject(CelestialObjectType type, String texturePath, String scale, String posX, String posY, String posZ, String distance, String degreesX, String degreesY, String degreesZ, String baseDegreesX, String baseDegreesY, String baseDegreesZ, CelestialObjectProperties celestialObjectProperties, String parent, String dimension, ColorEntry color, ArrayList<Util.VertexPoint> vertexList, SkyBoxObjectProperties skyBoxProperties) {
        this.type = type;
        if (parent != null) {
            CelestialObject o = createSkyObjectFromJson(CelestialSky.getFile("celestial:sky/" + dimension + "/objects/" + parent + ".json"), parent, dimension);
            this.posX = o.posX + "+" + posX;
            this.posY = o.posY + "+" + posY;
            this.posZ = o.posZ + "+" + posZ;
            this.distance = o.distance + "+" + distance;
            this.degreesX = o.degreesX + "+" + degreesX;
            this.degreesY = o.degreesY + "+" + degreesY;
            this.degreesZ = o.degreesZ + "+" + degreesZ;
            this.baseDegreesX = o.baseDegreesX + "+" + baseDegreesX;
            this.baseDegreesY = o.baseDegreesY + "+" + baseDegreesY;
            this.baseDegreesZ = o.baseDegreesZ + "+" + baseDegreesZ;
        }
        else {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.distance = distance;
            this.degreesX = degreesX;
            this.degreesY = degreesY;
            this.degreesZ = degreesZ;
            this.baseDegreesX = baseDegreesX;
            this.baseDegreesY = baseDegreesY;
            this.baseDegreesZ = baseDegreesZ;
        }
        this.scale = scale;
        this.celestialObjectProperties = celestialObjectProperties;
        this.vertexList = vertexList;
        this.skyBoxProperties = skyBoxProperties;
        if (texturePath != null)
            this.texture = new ResourceLocation(texturePath);
        if (color != null)
            this.solidColor = color;
    }

    // Used for populate objects only
    public CelestialObject(CelestialObjectType type, ResourceLocation texture, String scale, double scaleAdd, double posX, double posY, double posZ, String distance, double distanceAdd, double degreesX, double degreesY, double degreesZ, String baseDegreesX, String baseDegreesY, String baseDegreesZ, CelestialObjectProperties celestialObjectProperties, ColorEntry color, ArrayList<Util.VertexPoint> vertexList) {
        this.type = type;
        this.texture = texture;
        this.scale = scale;
        this.populatePosX = posX;
        this.populatePosY = posY;
        this.populatePosZ = posZ;
        this.populateScaleAdd = scaleAdd;
        this.populateDistanceAdd = distanceAdd;
        this.distance = distance;
        this.populateDegreesX = degreesX;
        this.populateDegreesY = degreesY;
        this.populateDegreesZ = degreesZ;
        this.baseDegreesX = baseDegreesX;
        this.baseDegreesY = baseDegreesY;
        this.baseDegreesZ = baseDegreesZ;
        this.celestialObjectProperties = celestialObjectProperties;
        this.vertexList = vertexList;
        this.solidColor = color;
        this.skyBoxProperties = null;
    }

    public boolean isPopulation() {
        return false;
    }

    public static CelestialObject createSkyObjectFromJson(JsonObject o, String name, String dimension) {
        if (o == null) {
            Util.warn("Failed to load celestial object \"" + name + ".json\", as it did not exist.");
            return null;
        }

        CelestialObjectType type = findObjectType(o);

        if (type == CelestialObjectType.SKYBOX)
            return createSkyBoxObjectFromJson(o, name, dimension);

        JsonObject display = o.getAsJsonObject("display");
        JsonObject rotation = o.getAsJsonObject("rotation");
        //I love parameters
        CelestialObject object = new CelestialObject(
                type,
                Util.getOptionalString(o, "texture", null),
                Util.getOptionalString(display, "scale", "0"),
                Util.getOptionalString(display, "pos_x", "0"),
                Util.getOptionalString(display, "pos_y", "0"),
                Util.getOptionalString(display, "pos_z", "0"),
                Util.getOptionalString(display, "distance", "0"),
                Util.getOptionalString(rotation, "degrees_x", "0"),
                Util.getOptionalString(rotation, "degrees_y", "0"),
                Util.getOptionalString(rotation, "degrees_z", "0"),
                Util.getOptionalString(rotation, "base_degrees_x", "-90"),
                Util.getOptionalString(rotation, "base_degrees_y", "0"),
                Util.getOptionalString(rotation, "base_degrees_z", "-90"),
                CelestialObjectProperties.createCelestialObjectPropertiesFromJson(o.getAsJsonObject("properties")),
                Util.getOptionalString(o, "parent", null),
                dimension,
                ColorEntry.createColorEntry(o, "solid_color", null),
                Util.convertToPointUvList(o, "vertex"),
                null
        );

        //Check if it's normal
        if (!o.has("populate")) {
            return object;
        }
        //Or... it's not :(
        else {
            return CelestialPopulateProperties.getPopulationPropertiesFromJson(o.getAsJsonObject("populate")).generatePopulateObjects(object,
                    o.getAsJsonObject("populate"));
        }
    }

    public static CelestialObject createSkyBoxObjectFromJson(JsonObject o, String name, String dimension) {
        JsonObject display = o.getAsJsonObject("display");
        JsonObject rotation = o.getAsJsonObject("rotation");
        //I love parameters
        CelestialObject object = new CelestialObject(
                findObjectType(o),
                Util.getOptionalString(o, "texture", null),
                Util.getOptionalString(display, "scale", "0"),
                Util.getOptionalString(display, "pos_x", "0"),
                Util.getOptionalString(display, "pos_y", "0"),
                Util.getOptionalString(display, "pos_z", "0"),
                Util.getOptionalString(display, "distance", "0"),
                Util.getOptionalString(rotation, "degrees_x", "0"),
                Util.getOptionalString(rotation, "degrees_y", "0"),
                Util.getOptionalString(rotation, "degrees_z", "0"),
                Util.getOptionalString(rotation, "base_degrees_x", "-90"),
                Util.getOptionalString(rotation, "base_degrees_y", "0"),
                Util.getOptionalString(rotation, "base_degrees_z", "-90"),
                CelestialObjectProperties.createCelestialObjectPropertiesFromJson(o.getAsJsonObject("properties")),
                Util.getOptionalString(o, "parent", null),
                dimension,
                ColorEntry.createColorEntry(o, Util.getOptionalString(o, "solid_color", null), null),
                null,
                SkyBoxObjectProperties.getSkyboxPropertiesFromJson(o)
        );
        return object;
    }

    public enum CelestialObjectType {
        DEFAULT,
        COLOR,
        SKYBOX
    }

    public static CelestialObjectType findObjectType(JsonObject o) {
        String objectType = Util.getOptionalString(o, "type", "default");
        if (o.has("texture") && !objectType.equals("skybox"))
            return CelestialObjectType.DEFAULT;
        else if (o.has("solid_color") && !objectType.equals("skybox"))
            return CelestialObjectType.COLOR;
        return getCelestialObjectType(objectType);
    }
    public static CelestialObjectType getCelestialObjectType(String i) {
        switch (i) {
            case "color":
                return CelestialObjectType.COLOR;
            case "skybox":
                return CelestialObjectType.SKYBOX;
            default:
                return CelestialObjectType.DEFAULT;
        }
    }
}
