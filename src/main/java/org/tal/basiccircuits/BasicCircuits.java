package org.tal.basiccircuits;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.circuit.CircuitLibrary;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends JavaPlugin {
    public static final Logger logger = Logger.getLogger("Minecraft");

    public BasicCircuits() {
        new CircuitLibrary() {

            @Override
            public Class[] getCircuitClasses() {
                return new Class[] { adder.class, and.class, clock.class, counter.class, demultiplexer.class, divider.class, flipflop.class,
                        multiplexer.class, multiplier.class, or.class, pisoregister.class, print.class, random.class, receiver.class,
                        shiftregister.class, transmitter.class, xor.class, decoder.class, encoder.class, pixel.class, pulse.class, not.class,
                        synth.class, srnor.class, terminal.class, router.class, ringcounter.class, iptransmitter.class, ipreceiver.class,
                        comparator.class, delay.class, repeater.class, nand.class, nor.class, xnor.class, segdriver.class };
            }

            @Override
            public void onRedstoneChipsEnable() {
                // add a new pref key for iptransmitter.
                redstoneChips.getPrefsManager().registerCircuitPreference(iptransmitter.class, "ports", "25600..25699");
            }
        };
    }

    @Override
    public void onDisable() {}

    @Override
    public void onEnable() {
    }
    
}
