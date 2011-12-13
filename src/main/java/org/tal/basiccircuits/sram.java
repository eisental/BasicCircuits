
package org.tal.basiccircuits;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.command.CommandUtils;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.util.Range;

/**
 *
 * @author Tal Eisenberg
 */
public class sram extends Circuit implements rcTypeReceiver {
    Memory memory;
    int addressLength;
    int wordLength;
    int readWritePin;
    int disablePin;
    int addressPin;
    int dataPin;
    
    boolean readOnlyMode;

    boolean sramDisable = false;
    boolean readWrite = false;

    boolean anonymous = true;
    String memId;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==readWritePin) {
            readWrite = state;
            BitSet7 data = inputBits.get(dataPin, dataPin+wordLength);

            if (readWrite && !sramDisable) { // store current data inputs when readWrite goes high.
                BitSet7 address = inputBits.get(addressPin, addressPin+addressLength);
                if (hasDebuggers()) debug("Writing " + BitSetUtils.bitSetToBinaryString(data, 0, wordLength) + " to address " + BitSetUtils.bitSetToUnsignedInt(address, 0, addressLength));
                memory.write(address, data);
            } else {
                this.sendBitSet(data);
            }
        } else if (inIdx==disablePin) {
            sramDisable = state;
            if (hasDebuggers()) debug("Chip " + (sramDisable?"disabled.":"enabled"));
            if (sramDisable) {
                outputBits.clear();
            } else {
                if (readWrite) readMemory();
                else sendBitSet(inputBits.get(dataPin, dataPin+wordLength));
            }

            sendBitSet(outputBits);
        } else if (inIdx>=addressPin && inIdx<addressPin+addressLength) {
            if (readWrite && !sramDisable) {
                readMemory();
            }
        } else if (inIdx>=dataPin && inIdx<dataPin+wordLength) {
            if (!readWrite && !sramDisable) {
                // copy data inputs to outputs
                sendBitSet(inputBits.get(dataPin, dataPin+wordLength));
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        wordLength = outputs.length;

        if (args.length>1 && args[1].equalsIgnoreCase("readonly")) {
            readOnlyMode = true;
            addressLength = inputs.length-1;
            addressPin = 1;
            disablePin = 0;
            dataPin = -1;
            readWritePin = -1;
            readWrite = true;
        } else {
            readOnlyMode = false;
            addressLength = inputs.length-2-wordLength;
            addressPin = 2;
            disablePin = 1;
            readWritePin = 0;
            dataPin = addressPin + addressLength;
        }

        if (outputs.length==0) {
            error(sender, "Exepcting at least 1 output pin.");
        }

        if (addressLength<1) {
            if (readOnlyMode)
                error(sender, "Expecting at least 1 control pin, and 1 address input pin.");
            else
                error(sender, "Expecting at least 2 control pins, 1 address input pin, and " + wordLength + " data pins.");

            return false;
        }

        if (args.length>0) {
            if (!isLegalId(args[0])) {
                error(sender, "Bad memory id: " + args[0]);
                return false;
            } else
                memId = args[0];

            anonymous = false;
        } else {
            memId = findFreeRamID();
            anonymous = true;
        }

        if (!Memory.memories.containsKey(memId)) {
            memory = new Ram();
            memory.init(memId);

            File file = getMemoryFile(memId);

            try {
                if (file.exists()) {
                    memory.load(file);
                } else {
                    file.createNewFile();
                }
            } catch (IOException ex) {
                String msg = "While creating new memory file: " + ex;
                if (sender != null)
                    info(sender, msg);
                else redstoneChips.log(Level.WARNING, msg);
            }

        } else memory = Memory.memories.get(memId);

        if (!readOnlyMode) {
            readWrite = inputBits.get(readWritePin);
        }
        
        sramDisable = inputBits.get(disablePin);
        
        redstoneChips.registerRcTypeReceiver(activationBlock, this);
        if (sender!=null) resetOutputs();
        if (readWrite && !sramDisable) {
            readMemory();
        }
        
        info(sender, "This sram chip can hold up to " + (int)Math.pow(2, addressLength) + "x" + wordLength + " bits. Memory data will be stored at " + ChatColor.YELLOW + getMemoryFile(memId).getPath());

        return true;
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
                redstoneChips.log(Level.SEVERE, "Could not delete memory file: " + data);
            }
        }
    }

    @Override
    public void save() {
         // store data in file.
        File file = getMemoryFile(memId);

        try {
            memory.store(file);
        } catch (IOException ex) {
            redstoneChips.log(Level.SEVERE, "While saving memory to file: " + ex.getMessage());
        }
    }

    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) return;
        int curIdx = 0;

        if (words[0].equalsIgnoreCase("ascii")) {
            StringBuilder b = new StringBuilder();
            for (int i=1; i<words.length; i++)
                b.append(words[i]);

            String ascii = b.toString();
            for (int i=0; i<ascii.length(); i++)
                memory.write(BitSetUtils.intToBitSet(i, addressLength), BitSetUtils.intToBitSet((int)ascii.charAt(i), wordLength));
        } else if (words[0].equalsIgnoreCase("notes")) {
        } else if (words[0].equalsIgnoreCase("dump")) {
            if (words.length==1) {
                dumpMemory(player, null);
            } else {
                try {
                    dumpMemory(player, words[1]);
                } catch (IllegalArgumentException ie) {
                    player.sendMessage("Bad range argument: " + words[1]);
                }
            }

        } else {
            for (String word : words) {
                // either idx:value or just value
                int colonIdx = word.indexOf(":");
                try {
                    if (colonIdx==-1) {
                        // use running index
                        BitSet7 value = parseData(player, word);
                        if (value==null) return;
                        memory.write(BitSetUtils.intToBitSet(curIdx, addressLength), value);
                        curIdx++;
                    } else {
                        int address = Integer.decode(word.substring(0, colonIdx));
                        BitSet7 value = parseData(player, word.substring(colonIdx+1));
                        if (value==null) return;
                        memory.write(BitSetUtils.intToBitSet(address, addressLength), value);
                    }
                } catch (NumberFormatException ne) {
                    error(player, "Bad entry. Expecting either a value or <address>:<value> - " + word);
                    return;
                }
            }
            info(player, "Successfully written to memory.");
        }

        
    }

    private void readMemory() {
        BitSet7 address = inputBits.get(addressPin, addressPin+addressLength);
        BitSet7 data = memory.read(address);
        if (hasDebuggers()) debug("Reading " + BitSetUtils.bitSetToBinaryString(data, 0, wordLength) + " from address " + BitSetUtils.bitSetToUnsignedInt(address, 0, addressLength));
        sendBitSet(0, wordLength, data);
    }

    private BitSet7 parseData(CommandSender sender, String data) {
        try {
            int ret = Integer.decode(data);
            return BitSetUtils.intToBitSet(ret, wordLength);
        } catch (NumberFormatException ne) {
            if (data.length()==1) return BitSetUtils.intToBitSet((int)data.charAt(0), wordLength);
            else if (data.startsWith("b")) {
                String bits = data.substring(1);
                return BitSetUtils.stringToBitSet(bits);
            } else {
                error(sender, "Bad data: " + data + ". Expecting either a number or 1 ascii character.");
                return null;
            }
        }
    }

    private boolean isLegalId(String string) {
        return string.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }


    @Override
    protected boolean isStateless() {
        return false;
    }

    private void dumpMemory(Player player, String srange) {
        int firstAddress, lastAddress;

        if (srange==null) {
            firstAddress = 0;
            lastAddress = (int)Math.pow(2, addressLength)-1;
        } else {
            Range range = new Range(srange, Range.Type.OPEN_ALLOWED);
            firstAddress = (int)(range.hasLowerLimit()?range.getOrderedRange()[0]:0);
            lastAddress = (int)(range.hasUpperLimit()?range.getOrderedRange()[1]:Math.pow(2, addressLength));
        }

        String lines = "";

        if (firstAddress>=0 && lastAddress>=0) {
            for (int a = firstAddress; a<=lastAddress; a++) {
                String value;
                String address = zeroPad(a, (int)Math.pow(2, addressLength)-1);
                BitSet7 data = memory.read(BitSetUtils.intToBitSet(a, addressLength));
                if (wordLength>32) value = Integer.toHexString(BitSetUtils.bitSetToSignedInt(data, 0, wordLength));
                else value = BitSetUtils.bitSetToBinaryString(data, 0, wordLength);
                lines += ChatColor.YELLOW.toString() + address + ": " + ChatColor.WHITE + value + "\n";
            }
            
            String titleRange;
            if (firstAddress==lastAddress)
                titleRange = Integer.toString(firstAddress);
            else titleRange = firstAddress + "-" + lastAddress;
            
            CommandUtils.pageMaker(player, "sram " + memId + " memory (" + titleRange + ")", "rctype dump", lines, redstoneChips.getPrefs().getInfoColor(), redstoneChips.getPrefs().getErrorColor());
        } else {
            player.sendMessage(redstoneChips.getPrefs().getErrorColor() + "Invalid address range: " + firstAddress + ".." + lastAddress);
        }
    }
    
    private String zeroPad(int a, int max) {
        String pad = "";
        String address = Integer.toString(a);
        int charCount = Integer.toString(max).length();
        for (int i=0; i<charCount-address.length(); i++) pad += "0";
        return pad + address;
    }
}
