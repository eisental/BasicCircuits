
package org.tal.basiccircuits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSet7Constructor;
import org.tal.redstonechips.util.BitSet7Representer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a memory capable of reading and writing bit sets.
 *
 * @author Tal Eisenberg
 */
public abstract class Memory {
    public static Map<String,Memory> memories = new HashMap<String,Memory>();

    private String id;

    public abstract BitSet7 read(BitSet7 address);

    public abstract void write(BitSet7 address, BitSet7 data);

    public void init(String id) {
        this.id = id;
        memories.put(id, this);
    }

    public String getId() { return id; }

    public void store(File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new BitSet7Representer(), options);
        yaml.dump(getData(), new FileWriter(file));
    }

    public void load(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml(new BitSet7Constructor());
        setData((Map)yaml.load(new FileInputStream(file)));
    }

    protected abstract Map getData();

    protected abstract void setData(Map data);
}
