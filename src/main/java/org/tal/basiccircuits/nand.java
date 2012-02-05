
package org.tal.basiccircuits;

import org.tal.redstonechips.circuit.BitSetCircuit;
import org.tal.redstonechips.bitset.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class nand extends BitSetCircuit {

    @Override
    protected void bitSetChanged(int bitSetidx, BitSet7 set) {
        BitSet7 out = (BitSet7)inputBitSets[0].clone();

        for (int i=1; i<this.inputBitSets.length; i++) {
            out.and(inputBitSets[i]);
        }
        
        out.flip(0, wordlength);
        this.sendBitSet(out);

    }

}
