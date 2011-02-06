package org.tal.basiccircuits;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.tal.redstonechips.CircuitLibrary;
import org.tal.redstonechips.util.TargetBlock;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends CircuitLibrary {

    public static List<transmitter> transmitters = new ArrayList<transmitter>();
    public static List<receiver> receivers = new ArrayList<receiver>();
    public static Map<Location, terminal> terminals = new HashMap<Location, terminal>();

    public BasicCircuits(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    @Override
    public void onRedstoneChipsEnable() {
        // add a new pref key for iptransmitter.
        redstoneChips.getPrefsManager().registerCircuitPreference(iptransmitter.class, "ports", "25600..25699");
    }

    @Override
    public Class[] getCircuitClasses() {
        return new Class[] { adder.class, and.class, clock.class, counter.class, demultiplexer.class, divider.class, flipflop.class,
                multiplexer.class, multiplier.class, or.class, pisoregister.class, print.class, random.class, receiver.class,
                shiftregister.class, transmitter.class, xor.class, decoder.class, encoder.class, pixel.class, pulse.class, not.class,
                synth.class, srnor.class, terminal.class, router.class, decadecounter.class, iptransmitter.class, ipreceiver.class,
                comparator.class };
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-channels")) {
            SortedSet<String> channels = new TreeSet<String>();
            for (transmitter t : transmitters) channels.add(t.getChannel());
            for (receiver r : receivers) channels.add(r.getChannel());
            if (channels.isEmpty()) {
                sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + "There are no registered channels.");
            } else {
                sender.sendMessage("");
                sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + "Currently used broadcast channels:");
                sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + "------------------------------");
                String list = "";
                ChatColor color = ChatColor.WHITE;
                for (String channel : channels) {
                    list += color + channel + ", ";
                    if (list.length()>50) {
                        sender.sendMessage(list.substring(0, list.length()-2));
                        list = "";
                    }
                    if (color==ChatColor.WHITE)
                        color = ChatColor.YELLOW;
                    else color = ChatColor.WHITE;
                }
                if (!list.isEmpty()) sender.sendMessage(list.substring(0, list.length()-2));
                sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + "Used by " + transmitters.size() + " transmitter(s) and " + receivers.size() + " receiver(s).");
                sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + "------------------------------");
                sender.sendMessage("");
            }
            
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-type")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players are allowed to run this command.");
            }
            Player player = (Player)sender;
            TargetBlock b = new TargetBlock(player);
            terminal t = terminals.get(b.getTargetBlock().getLocation());
            if (t==null) {
                player.sendMessage(redstoneChips.getPrefsManager().getErrorColor() + "You must point towards a terminal screen (a terminal circuit's interface block) to type anything.");
            } else {
                t.type(args, player);
            }
            return true;
        } else return false;
    }
}
