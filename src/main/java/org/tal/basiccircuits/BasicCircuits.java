/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
    RedstoneChips plugin;
    PluginDescriptionFile pdf;

    public static List<transmitter> transmitters = new ArrayList<transmitter>();
    public static List<receiver> receivers = new ArrayList<receiver>();
    public static final String rcName = "RedstoneChips";
    static final Logger log = Logger.getLogger("Minecraft");

    public BasicCircuits(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        this.pdf = desc;
    }

    @Override
    public void onDisable() {
        log.info(pdf.getName() + " " + pdf.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        Plugin p = getServer().getPluginManager().getPlugin(rcName);
        if (p==null) {
            log.warning(pdf.getName() + " " + pdf.getVersion() + ": Required plugin " + rcName + " is missing.");
        }

        plugin = (RedstoneChips)p;

        plugin.addCircuitClass(adder.class);
        plugin.addCircuitClass(and.class);
        plugin.addCircuitClass(clock.class);
        plugin.addCircuitClass(counter.class);
        plugin.addCircuitClass(demultiplexer.class);
        plugin.addCircuitClass(divider.class);
        plugin.addCircuitClass(flipflop.class);
        plugin.addCircuitClass(multiplexer.class);
        plugin.addCircuitClass(multiplier.class);
        plugin.addCircuitClass(or.class);
        plugin.addCircuitClass(pisoregister.class);
        plugin.addCircuitClass(print.class);
        plugin.addCircuitClass(random.class);
        plugin.addCircuitClass(receiver.class);
        plugin.addCircuitClass(shiftregister.class);
        plugin.addCircuitClass(transmitter.class);
        plugin.addCircuitClass(xor.class);
        plugin.addCircuitClass(decoder.class);
        plugin.addCircuitClass(encoder.class);
        plugin.addCircuitClass(pixel.class);
        plugin.addCircuitClass(iptransmitter.class);
        plugin.addCircuitClass(ipreceiver.class);
        plugin.addCircuitClass(pulse.class);
        plugin.addCircuitClass(not.class);

        log.info(pdf.getName() + " " + pdf.getVersion() + " enabled.");
    }

    @Override
    public boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-channels")) {
            SortedSet<String> channels = new TreeSet<String>();
            for (transmitter t : transmitters) channels.add(t.getChannel());
            for (receiver r : receivers) channels.add(r.getChannel());
            if (channels.isEmpty()) {
                player.sendMessage(RedstoneChips.infoColor + "There are no registered channels.");
            } else {
                player.sendMessage("");
                player.sendMessage(RedstoneChips.infoColor + "Currently used broadcast channels:");
                player.sendMessage(RedstoneChips.infoColor + "------------------------------");
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
                player.sendMessage(RedstoneChips.infoColor + "Used by " + transmitters.size() + " transmitter(s) and " + receivers.size() + " receiver(s).");
                player.sendMessage(RedstoneChips.infoColor + "------------------------------");
                player.sendMessage("");
            }
            
            return true;
        } else return false;
    }
}
