
package org.tal.basiccircuits;

import org.tal.redstonechips.memory.Ram;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.RCTypeReceiver;
import org.tal.redstonechips.memory.Memory;
import org.tal.redstonechips.page.LineSource;
import org.tal.redstonechips.page.Pager;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.util.Range;

/**
 *
 * @author Tal Eisenberg
 */
public class sram extends Circuit implements RCTypeReceiver {

    Ram memory;
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
    
    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==readWritePin) {
            readWrite = state;
            BitSet7 data = inputBits.get(dataPin, dataPin+wordLength);

            if (readWrite && !sramDisable) { // store current data inputs when readWrite goes high.
                BitSet7 address = inputBits.get(addressPin, addressPin+addressLength);
                if (hasListeners()) debug("Writing " + BitSetUtils.bitSetToBinaryString(data, 0, wordLength) + " to address " + BitSetUtils.bitSetToUnsignedInt(address, 0, addressLength));
                memory.write(address, data);
            } else {
                this.sendBitSet(data);
            }
        } else if (inIdx==disablePin) {
            sramDisable = state;
            if (hasListeners()) debug("Chip " + (sramDisable?"disabled.":"enabled"));
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

        if (args.length>1 && (args[1].equalsIgnoreCase("readonly") || args[1].equalsIgnoreCase("rom"))) {
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
            error(sender, "Expecting at least 1 output pin.");
        }

        if (addressLength<1) {
            if (readOnlyMode)
                error(sender, "Expecting at least 1 control pin, and 1 address input pin.");
            else
                error(sender, "Expecting at least 2 control pins, 1 address input pin, and " + wordLength + " data pins.");

            return false;
        }
        
        try {
            if (args.length>0) {
                anonymous = false;
                // if new memory subclasses are added there should be a check here for memory class.
                memory = (Ram)Memory.getMemory(args[0], Ram.class);
            } else {
                anonymous = true;
                memory = (Ram)Memory.getMemory(args[0], Ram.class);
            }
        } catch (IOException ex) {
            error(sender, "While creating new memory file: " + ex);
            return false;
        } catch (IllegalArgumentException e) {
            error(sender, e.getMessage());
            return false;
        }

        
        if (!readOnlyMode) {
            readWrite = inputBits.get(readWritePin);
        }
        
        sramDisable = inputBits.get(disablePin);
        
        redstoneChips.addRCTypeReceiver(activationBlock, this);
        if (sender!=null) resetOutputs();
        if (readWrite && !sramDisable) {
            readMemory();
        }
        
        info(sender, "This sram chip can hold up to " + (int)Math.pow(2, addressLength) + "x" + wordLength + " bits. Memory data will be stored at " + ChatColor.YELLOW + memory.getFile().getPath());
        
        return true;
    }

    @Override
    public void circuitDestroyed() {
        if (anonymous) {
            if (!memory.delete()) {
                redstoneChips.log(Level.SEVERE, "Could not delete memory file: " + memory.getFile());
            }
        }
    }

    @Override
    public void save() {
        try {
            memory.save();
        } catch (IOException ex) {
            redstoneChips.log(Level.SEVERE, "While saving memory to file: " + ex.getMessage());
        }
    }

    private void readMemory() {
        BitSet7 address = inputBits.get(addressPin, addressPin+addressLength);
        BitSet7 data = memory.read(address);
        if (hasListeners()) debug("Reading " + BitSetUtils.bitSetToBinaryString(data, 0, wordLength) + " from address " + BitSetUtils.bitSetToUnsignedInt(address, 0, addressLength));
        sendBitSet(0, wordLength, data);
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

        if (firstAddress>=0 && lastAddress>=0) {
            String titleRange;
            if (firstAddress==lastAddress)
                titleRange = Integer.toString(firstAddress);
            else titleRange = firstAddress + "-" + lastAddress;
            
            MemoryLineSource l = new MemoryLineSource(firstAddress, lastAddress-firstAddress+1);
            
            Pager.beginPaging(player, "sram " + memory.getId() + " memory (" + titleRange + ")", 
                    l, redstoneChips.getPrefs().getInfoColor(), redstoneChips.getPrefs().getErrorColor());
        } else {
            player.sendMessage(redstoneChips.getPrefs().getErrorColor() + "Invalid address range: " + firstAddress + ".." + lastAddress);
        }
    }
            
    @Override
    protected boolean isStateless() {
        return false;
    }
    
    class MemoryLineSource implements LineSource {
        int offset;
        int length;
        
        public MemoryLineSource(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
        
        @Override
        public String getLine(int idx) {
            String value;
            String address = zeroPad(idx+offset, (int)Math.pow(2, addressLength)-1);
            BitSet7 data = memory.read(BitSetUtils.intToBitSet(idx+offset, addressLength));
            if (wordLength>32) value = Integer.toHexString(BitSetUtils.bitSetToSignedInt(data, 0, wordLength));
            else value = BitSetUtils.bitSetToBinaryString(data, 0, wordLength);
            return ChatColor.YELLOW.toString() + address + ": " + ChatColor.WHITE + value + "\n";
        }

        @Override
        public int getLineCount() {
            return length;
        }
        
        private String zeroPad(int a, int max) {
            String pad = "";
            String address = Integer.toString(a);
            int charCount = Integer.toString(max).length();
            for (int i=0; i<charCount-address.length(); i++) pad += "0";
            return pad + address;
        }
        
        
    }
    
    
}
