package org.tal.basiccircuits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.RedstoneChips;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends JavaPlugin {
    private RedstoneChips rc;


    public static List<transmitter> transmitters = new ArrayList<transmitter>();
    public static List<receiver> receivers = new ArrayList<receiver>();
    public static final String rcName = "RedstoneChips";
    static final Logger log = Logger.getLogger("Minecraft");

    public BasicCircuits(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    @Override
    public void onDisable() {
        log.info(getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        Plugin p = getServer().getPluginManager().getPlugin(rcName);
        if (p==null) {
            log.warning(getDescription().getName() + " " + getDescription().getVersion() + ": Required plugin " + rcName + " is missing.");
        }

        rc = (RedstoneChips)p;
        try {
            rc.addCircuitClasses(adder.class, and.class, clock.class, counter.class, demultiplexer.class, divider.class, flipflop.class,
                    multiplexer.class, multiplier.class, or.class, pisoregister.class, print.class, random.class, receiver.class,
                    shiftregister.class, transmitter.class, xor.class, decoder.class, encoder.class, pixel.class, iptransmitter.class,
                    ipreceiver.class, pulse.class, not.class, synth.class);
        } catch (NoClassDefFoundError ncde) {
            log.log(Level.SEVERE, getDescription().getName() + ": Can't find RedstoneChips plugin or version mismatch.");
        }
        
        log.info(getDescription().getName() + " " + getDescription().getVersion() + " circuit package enabled.");
    }

    @Override
    public boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-channels")) {
            SortedSet<String> channels = new TreeSet<String>();
            for (transmitter t : transmitters) channels.add(t.getChannel());
            for (receiver r : receivers) channels.add(r.getChannel());
            if (channels.isEmpty()) {
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no registered channels.");
            } else {
                player.sendMessage("");
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "Currently used broadcast channels:");
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "------------------------------");
                String list = "";
                ChatColor color = ChatColor.WHITE;
                for (String channel : channels) {
                    list += color + channel + ", ";
                    if (list.length()>50) {
                        player.sendMessage(list.substring(0, list.length()-2));
                        list = "";
                    }
                    if (color==ChatColor.WHITE)
                        color = ChatColor.YELLOW;
                    else color = ChatColor.WHITE;
                }
                if (!list.isEmpty()) player.sendMessage(list.substring(0, list.length()-2));
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "Used by " + transmitters.size() + " transmitter(s) and " + receivers.size() + " receiver(s).");
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "------------------------------");
                player.sendMessage("");
            }
            
            return true;
        } else return false;
    }
}
