/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.util.HashMap;
import java.util.Map;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class Ram extends Memory {
    private Map<BitSet7, BitSet7> words;

    @Override
    public BitSet7 read(BitSet7 address) {
        return words.get(address);
    }

    @Override
    public void write(BitSet7 address, BitSet7 data) {
        words.put(address, data);
    }

    @Override
    public void init(String id) {
        super.init(id);
        words = new HashMap<BitSet7, BitSet7>();
    }

    @Override
    protected Map getData() {
        return words;
    }

    @Override
    protected void setData(Map data) {
        words.clear();

        if (data==null) return;
        
        for (Object key : data.keySet()) {            
            Object value = data.get(key);
            
            BitSet7 address = convert(key);
            BitSet7 word = convert(value);
            words.put(address, word);
        }
    }

    private BitSet7 convert(Object obj) {
        if (obj instanceof BitSet7) return (BitSet7)obj;
        else if (obj instanceof Integer) return BitSetUtils.intToBitSet((Integer)obj, 32);
        else throw new IllegalArgumentException("Unsupported memory data class: " + obj.getClass().getCanonicalName());
    }
}
