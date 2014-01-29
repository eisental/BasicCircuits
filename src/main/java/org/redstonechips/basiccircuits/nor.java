
package org.redstonechips.basiccircuits;

import org.redstonechips.circuit.BitSetCircuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class nor extends BitSetCircuit {
    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        boolean[] buf = inputBitSets[0].clone();
        
        for (int i=1; i<this.inputBitSets.length; i++) {
            BooleanArrays.or(buf, buf, inputBitSets[i]);
        }

        // not gate after series of ors
        BooleanArrays.not(buf, buf);

        this.writeBits(buf);
    }
}
