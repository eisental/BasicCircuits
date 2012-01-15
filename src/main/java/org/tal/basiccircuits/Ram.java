
package org.tal.basiccircuits;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class Ram extends Memory {
    public static File dataFolder;
    private Map<BitSet7, BitSet7> words;
    
    public static Ram getRam(String memId) throws IOException {
        if (!Ram.isValidId(memId)) throw new IllegalArgumentException("Invalid memory id: " + memId);
        
        if (!Memory.memories.containsKey(memId)) {
            Ram memory = new Ram();
            memory.init(memId);

            File file = Ram.getMemoryFile(memId);
            
            if (file.exists()) {
                memory.load(file);
            } else {
                file.createNewFile();
            }
            
            return memory;
        } else return (Ram)Memory.memories.get(memId);
        
    }
    
    public static Ram getRam() throws IOException {
        return  Ram.getRam(Ram.getFreeRamID());
    }
    
    @Override
    public BitSet7 read(BitSet7 address) {
        BitSet7 data = words.get(address);
        if (data==null) data = new BitSet7();
        return data;
    }

    public BitSet7 read(int address) {
        BitSet7 data = words.get(BitSetUtils.intToBitSet(address, 32));
        if (data==null) data = new BitSet7();
        return data;
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
    
    public void init() {
        init(Ram.getFreeRamID());
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

    public void save() throws IOException {
        store(getMemoryFile(getId()));
    }
    
    private BitSet7 convert(Object obj) {
        if (obj instanceof BitSet7) return (BitSet7)obj;
        else if (obj instanceof Integer) return BitSetUtils.intToBitSet((Integer)obj, 32);
        else throw new IllegalArgumentException("Unsupported memory data class: " + obj.getClass().getCanonicalName());
    }
    
    public static File getMemoryFile(String id) {
        return new File(dataFolder, "sram-" + id + ".data");
    }
    
    public static String getFreeRamID() {
        File file;
        int idx = 0;

        do {
            file = getMemoryFile(Integer.toString(idx));
            idx++;
        } while (file.exists());
        return Integer.toString(idx);
    }
    
    public static boolean setupDataFolder(File pluginFolder) {
        dataFolder = new File(pluginFolder, "sram");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) 
                throw new RuntimeException("Can't make folder " + dataFolder.getAbsolutePath());
            else return true;
        } else {
            return false;
        }        
    }

    public boolean delete() {
        return getFile().delete();
    }

    public File getFile() {
        return Ram.getMemoryFile(getId());
    }        
}
