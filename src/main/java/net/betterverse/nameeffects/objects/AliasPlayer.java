package net.betterverse.nameeffects.objects;

import java.util.LinkedHashMap;
import java.util.Map;
import net.betterverse.nameeffects.NameEffects;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class AliasPlayer implements ConfigurationSerializable {

    private String name;
    private String alias = null, prefix = null;

    public AliasPlayer(String name, String alias, String prefix) {
        this.name = name;
        this.alias = alias;
        this.prefix = prefix;
    }

    public AliasPlayer(String name) {
        this.name = name;
        prefix = "";
        alias = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        update();
    }

    public void resetAlias() {
        this.alias = null;
        update();
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        update();
    }

    public void resetPrefix() {
        this.prefix = null;
        update();
    }

    protected void update() {
        NameEffects.getInstance().players.put(this.name, this);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("prefix", prefix);
        result.put("alias", alias);
        return result;
    }

    public static AliasPlayer deserialize(Map<String, Object> args) {
        return new AliasPlayer(null, (String) args.get("alias"), (String) args.get("prefix"));
    }
}
