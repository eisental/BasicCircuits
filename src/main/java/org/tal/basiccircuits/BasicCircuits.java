package org.tal.basiccircuits;

import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.CircuitLibrary;

/**
 *
 * @author Tal Eisenberg
 */
public class BasicCircuits extends CircuitLibrary {
    @Override
    public Class[] getCircuitClasses() {
        return new Class[] { adder.class, and.class, clock.class, counter.class, demultiplexer.class, divider.class, flipflop.class,
                multiplexer.class, multiplier.class, or.class, pisoregister.class, print.class, random.class, receiver.class,
                shiftregister.class, transmitter.class, xor.class, decoder.class, encoder.class, pixel.class, pulse.class, not.class,
                synth.class, srnor.class, terminal.class, router.class, ringcounter.class, iptransmitter.class, ipreceiver.class,
                comparator.class, delay.class, repeater.class, nand.class, nor.class, xnor.class, segdriver.class };
    }

    @Override
    public void onRedstoneChipsEnable(RedstoneChips instance) {
        
        // add a new pref key for iptransmitter.
        instance.getPrefsManager().registerCircuitPreference(iptransmitter.class, "ports", "25600..25699");
    }
}
