package net.betterverse.alias;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Alias extends JavaPlugin {
    private File dataDir;
    public ArrayList<String> bannedAliases = new ArrayList<String>();
    public File playerDir;
    public HashMap<String, String> aliases;

    public static void print(String s) {
        System.out.println("Alias - " + s);
    }

    public void onEnable() {
        getCommand("alias").setExecutor(new AliasCommand(this));

        PluginManager pm = getServer().getPluginManager();
        AliasListener listener = new AliasListener(this);
        pm.registerEvents(listener, this);

        this.dataDir = getDataFolder();

        if (!this.dataDir.exists())
            this.dataDir.mkdir();

        File bannedFile = new File(this.dataDir.getAbsolutePath() + File.separator + "banned.txt");
        if (!bannedFile.exists())
            try {
                bannedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            FileReader j = new FileReader(this.dataDir.getAbsolutePath() + File.separator + "banned.txt");
            BufferedReader in = new BufferedReader(j);
            String l = in.readLine();
            while (l != null) {
                bannedAliases.add(l);
                l = in.readLine();
            }
            j.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            print("Failed to load banned aliases!");
        }

        aliases = new HashMap<String, String>();

        File aliasFile = new File(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
        if (!aliasFile.exists())
            try {
                aliasFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (loadAliases())
            print("Alias file loaded.");
        else {
            print("Alias file could not be loaded.");
        }
    }

    public void onDisable() {
        if (saveAliases())
            print("Aliases written to file.");
        else {
            print("Aliases could not be writtren to file.");
        }
    }

    protected boolean loadAliases() {
        try {
            FileReader k = new FileReader(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
            BufferedReader reader = new BufferedReader(k);
            String l = reader.readLine();
            while (l != null) {
                String[] values = l.split("=");
                if (values.length == 2)
                    this.aliases.put(values[0], values[1]);
                l = reader.readLine();
            }
            reader.close();
            k.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
	protected boolean saveAliases() {
        try {
            FileWriter l = new FileWriter(this.dataDir.getAbsolutePath() + File.separator + "aliases.txt");
            BufferedWriter writer = new BufferedWriter(l);
            for (Map.Entry entry : this.aliases.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
            writer.close();
            l.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean isPlayerName(String alias) {
        if ((this.playerDir != null) && (this.playerDir.exists())) {
            String[] playerArray = this.playerDir.list();
            for (String aPlayerArray : playerArray) {
                String loginName = aPlayerArray;
                loginName = loginName.replaceAll("\\.dat", "");
                if (alias.compareToIgnoreCase(loginName) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isBanned(String alias) {
        if (this.bannedAliases != null) {
            for (String notAllowed : this.bannedAliases) {
                if (alias.equals(notAllowed)) {
                    return true;
                }
            }
        }
        return false;
    }
}