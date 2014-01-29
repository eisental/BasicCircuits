
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class nand extends BitSetCircuit {

    @Override
    protected void bitSetChanged(int bitSetidx, boolean[] set) {
        boolean[] buf = inputBitSets[0].clone();

        for (int i=1; i<this.inputBitSets.length; i++) {
            BooleanArrays.and(buf, buf, inputBitSets[i]);
        }

        BooleanArrays.not(buf, buf);

        this.writeBits(buf);

    }
}
