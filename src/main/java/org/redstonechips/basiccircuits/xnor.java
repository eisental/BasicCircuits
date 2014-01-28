
package org.redstonechips.basiccircuits;

import org.redstonechips.chip.BitSetCircuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class xnor extends BitSetCircuit {

    @Override
    protected void bitSetChanged(int bitSetIdx, boolean[] set) {
        boolean[] buf = inputBitSets[0].clone();
        for (int i=1; i<this.inputBitSets.length; i++) {
            BooleanArrays.xor(buf, buf, inputBitSets[i]);
        }
        
        BooleanArrays.not(buf, buf);

        this.writeBits(buf);
    }
}
