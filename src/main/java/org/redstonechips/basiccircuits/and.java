package org.redstonechips.basiccircuits;


import org.redstonechips.chip.BitSetCircuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class and extends BitSetCircuit {

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        boolean[] buf = inputBitSets[0].clone();

        for (int i=1; i<this.inputBitSets.length; i++) {
            BooleanArrays.and(buf, buf, inputBitSets[i]);
        }
        
        this.writeBits(buf);
    }
}
