package org.tal.basiccircuits;

import java.io.File;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.tal.redstonechips.circuit.CircuitLibrary;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends CircuitLibrary {

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
                synth.class, srnor.class, terminal.class, router.class, ringcounter.class, iptransmitter.class, ipreceiver.class,
                comparator.class, delay.class };
    }
    
}
