/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Server;
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
        
        log.info(pdf.getName() + " " + pdf.getVersion() + " enabled.");
    }
}
