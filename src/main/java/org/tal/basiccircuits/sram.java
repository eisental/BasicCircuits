/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.util.BitSetUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Tal Eisenberg
 */
public class sram extends Circuit implements rcTypeReceiver {
    Map<Integer,Integer> memory;
    int addressLength;
    int wordLength;
    int readWritePin = 0;
    int disablePin = 1;

    int currentAddress = 0;
    int currentData = 0;

    boolean disabled = false;
    boolean readWrite = false;

    boolean anonymous = true;
    String memId;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==readWritePin) {
            readWrite = state;
            if (readWrite && !disabled) { // store current data inputs when readWrite goes high.
                if (hasDebuggers()) debug("Writing " + currentData + " at address 0x" + Integer.toHexString(currentAddress));
                memory.put(currentAddress, currentData);
            } else {
                this.sendInt(0, wordLength, currentData);
            }
        } else if (inIdx==disablePin) {
            disabled = state;
            if (disabled) {
                outputBits.clear();
            } else {
                if (readWrite) {
                    readMemory();
                } else {
                    sendInt(0, wordLength, currentData);
                }
            }
            sendBitSet(outputBits);
        } else if (inIdx>=2 && inIdx<2+addressLength) {
            currentAddress = BitSetUtils.bitSetToUnsignedInt(inputBits, 2, addressLength);
            if (readWrite && !disabled) {
                readMemory();
            }
        } else if (inIdx>=2+addressLength && inIdx<inputs.length) {
            currentData = BitSetUtils.bitSetToUnsignedInt(inputBits, 2+addressLength, wordLength);
            if (!readWrite && !disabled) {
                // copy data inputs to outputs
                sendInt(0, wordLength, currentData);
            }
        }
    }

    private void readMemory() {
        // update outputs according to address.
        Integer storedData = memory.get(currentAddress);
        if (storedData==null) storedData = 0;
        if (hasDebuggers()) debug("Reading " + storedData + " from address 0x" + Integer.toHexString(currentAddress));
        sendInt(0, wordLength, storedData);
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        wordLength = outputs.length;
        addressLength = inputs.length-2-wordLength;

        if (outputs.length==0) {
            error(sender, "Exepcting at least 1 output pin.");
        }
        if (addressLength<1) {
            error(sender, "Expecting at least 1 address input pin, 2 control pins and " + wordLength + " data pins.");
            return false;
        }

        if (memId==null) {
            if (args.length>0) {
                memId = args[0];
                anonymous = false;
            } else {
                memId = findFreeRamID();
                anonymous = true;
            }
        }

        memory = new HashMap<Integer, Integer>();
        currentAddress = BitSetUtils.bitSetToUnsignedInt(inputBits, 2, addressLength);
        currentData = BitSetUtils.bitSetToUnsignedInt(inputBits, 2+addressLength, wordLength);
        readWrite = inputBits.get(readWritePin);
        disabled = inputBits.get(disablePin);

        try {
            readMemoryFile();
        } catch (FileNotFoundException ex) {
            String msg = "Memory file " + getMemoryFile(memId) + " was not found. Creating a new one.";
            if (sender != null)
                info(sender, msg);
            else redstoneChips.log(Level.WARNING, msg);
        }
        
        redstoneChips.registerRcTypeReceiver(activationBlock, this);

        info(sender, "This sram chip can hold up to " + (int)Math.pow(2, addressLength) + "x" + wordLength + " bits. Memory data will be stored at " + ChatColor.YELLOW + getMemoryFile(memId).getPath());

        return true;
    }

    private void readMemoryFile() throws FileNotFoundException {
        File data = getMemoryFile(memId);
        Yaml yaml = new Yaml();

        memory = (Map<Integer,Integer>) yaml.load(new FileInputStream(data));
    }

    private File getMemoryFile(String id) {
        return new File(redstoneChips.getDataFolder(), "sram-" + id + ".data");
    }

    private String findFreeRamID() {
        File file;
        int idx = 0;

        do {
            file = getMemoryFile(Integer.toString(idx));
            idx++;
        } while (file.exists());
        return Integer.toString(idx);
    }

    @Override
    public void circuitDestroyed() {
        if (anonymous) {
            File data = getMemoryFile(memId);
            if (!data.delete()) {
                Logger.getLogger("Minecraft").severe("Could not delete memory file: " + data);
            }
        }
    }

    @Override
    public void save() {
         // store data in file.
        File data = getMemoryFile(memId);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        Yaml yaml = new Yaml(options);

        try {
            yaml.dump(memory, new FileWriter(data));
        } catch (IOException ex) {
            Logger.getLogger("Minecraft").severe("While saving memory to file: " + ex.getMessage());
        }
    }

    @Override
    public void type(String[] words, Player player) {
        int curIdx = 0;
        if (words.length>0 && words[0].equalsIgnoreCase("ascii")) {
            StringBuilder b = new StringBuilder();
            for (int i=1; i<words.length; i++)
                b.append(words[i]);

            String ascii = b.toString();
            for (int i=0; i<ascii.length(); i++)
                memory.put(i, (int)ascii.charAt(i));
        } else {
            for (String word : words) {
                // either idx:value or just value
                int colonIdx = word.indexOf(":");
                try {
                    if (colonIdx==-1) {
                        // use running index

                        int value = parseData(player, word);
                        memory.put(curIdx, value);
                        curIdx++;
                    } else {
                        int address = Integer.decode(word.substring(0, colonIdx));
                        int value = parseData(player, word.substring(colonIdx+1));
                        memory.put(address, value);
                    }
                } catch (NumberFormatException ne) {
                    error(player, "Bad entry. Expecting either an integer value or <address>:<int value> - " + word);
                    return;
                }
            }
        }

        info(player, "Successfully written to memory.");
    }

    private Integer parseData(CommandSender sender, String data) {
        try {
            int ret = Integer.decode(data);
            return ret;
        } catch (NumberFormatException ne) {
            if (data.length()!=1) {
                error(sender, "Bad data: " + data + ". Expecting either a number or 1 ascii character.");
                return null;
            } else {
                return (int)data.charAt(0);
            }
        }
    }
}
