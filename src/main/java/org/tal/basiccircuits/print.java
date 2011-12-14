package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.InterfaceBlock;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.util.Locations;


/**
 *
 * @author Tal Eisenberg
 */
public class print extends Circuit implements rcTypeReceiver {
    private final static int clockPin = 0;
    private final static int scrollPin = 2;
    private final static int clearPin = 1;

    enum Type {
        num, signed, unsigned, ascii, hex, oct, bin;
    }

    enum Display {
        replace, add, scroll
    }

    private Type type = Type.num;
    private Display display = Display.replace;

    private int dataPin = 1;
    private String[] lines = new String[4];
    private StringBuffer textBuffer = new StringBuffer();
    private SignUpdateTask signUpdateTask;
    private Location[] signList;
    
    private static final int LineSize = 15;

    int scrollPos = 0;

    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==clockPin && state) {
            String s = convertInputs();
            if (s!=null) {
                updateText(s);
                updateSigns();
            }
        } else if (inIdx==clearPin && state && (display==Display.scroll || display==Display.add)) {
            clearSign();
        } else if (inIdx==scrollPin && state && display==Display.scroll) {
            if (scrollPos>=textBuffer.length()-1)
                scrollPos = 0;
            else
                scrollPos++;
            
            prepScrollLines();

            updateSigns();
        }
    }

    private void updateText(String text) {
        if (display==Display.add) {
            add(text);
            prepWrapLines();
            if (textBuffer.length()>LineSize*4) textBuffer.setLength(0);
        } else if (display==Display.replace) {
            textBuffer.setLength(0);
            textBuffer.append(text);
            prepWrapLines();
        } else if (display==Display.scroll) {
            add(text);
            prepScrollLines();
        }
    }

    private String convertInputs() {
        String text = null;

        if (type==Type.num || type==Type.unsigned) {
            text = Integer.toString(BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, inputs.length-dataPin));
        } else if (type==Type.signed) {
            text = Integer.toString(BitSetUtils.bitSetToSignedInt(inputBits, dataPin, inputs.length-dataPin));
        } else if (type==Type.hex) {
            text = Integer.toHexString(BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, inputs.length-dataPin));
        } else if (type==Type.oct) {
            text = Integer.toOctalString(BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, inputs.length-dataPin));
        } else if (type==Type.bin) {
            text = BitSetUtils.bitSetToBinaryString(inputBits, dataPin, inputs.length-dataPin);
        } else if (type==Type.ascii) {
            char c = (char)BitSetUtils.bitSetToUnsignedInt(inputBits, dataPin, inputs.length-dataPin);
            if (!Character.isISOControl(c)) text = Character.toString(c);
            else if (hasDebuggers()) debug("Ignoring control character 0x" + Integer.toHexString(c));
        }

        return text;
    }

    private void add(String text) {
        if (type==Type.ascii || textBuffer.length()==0) {
            textBuffer.append(text);
        } else
            textBuffer.append(" ").append(text);
    }

    private void updateSigns() {
        if (hasDebuggers()) {
            debug("printing:");
            debug(lines[0]);
            debug(lines[1]);
            debug(lines[2]);
            debug(lines[3]);
        }

        for (Location l : signList) {
            Sign s = (Sign)l.getBlock().getState();
            s.setLine(0, lines[0]);
            s.setLine(1, lines[1]);
            s.setLine(2, lines[2]);
            s.setLine(3, lines[3]);
            s.update();
        }
        
        //redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, signUpdateTask);
    }

    private void prepWrapLines() {
        if (textBuffer.length()>LineSize*3) {
            String line4 = textBuffer.substring(LineSize*3);
            if (line4.length()>LineSize) line4 = line4.substring(0, LineSize);

            lines[0] = textBuffer.substring(0, LineSize);
            lines[1] = textBuffer.substring(LineSize, LineSize*2);
            lines[2] = textBuffer.substring(LineSize*2, LineSize*3);
            lines[3] = line4;
        } else if (textBuffer.length()>LineSize*2) {
            lines[0] = "";
            lines[1] = textBuffer.substring(0, LineSize);
            lines[2] = textBuffer.substring(LineSize, LineSize*2);
            lines[3] = textBuffer.substring(LineSize*2);
        } else if (textBuffer.length()>LineSize) {
            lines[0] = "";
            lines[1] = textBuffer.substring(0,LineSize);
            lines[2] = textBuffer.substring(LineSize);
            lines[3] = "";
        } else {
            lines[0] = "";
            lines[1] = textBuffer.toString();
            lines[2] = "";
            lines[3] = "";
        }
    }

    private void prepScrollLines() {
        String window;

        if (textBuffer.length()>LineSize) { // turn scrolling on
            int end = Math.min(scrollPos+LineSize, textBuffer.length());
            window = textBuffer.substring(scrollPos, end);
            if (window.length()<LineSize) {
                window += " " + textBuffer.substring(0, LineSize-window.length());
            }
        } else window = textBuffer.toString();

        lines[0] = "";
        lines[1] = window;
        lines[2] = "";
        lines[3] = "";
    }

    private void clearSign() {
        if (hasDebuggers()) debug("Clearing sign.");
        textBuffer.setLength(0);
        scrollPos = 0;
        lines[0] = "";
        lines[1] = "";
        lines[2] = "";
        lines[3] = "";
        updateSigns();
    }

    @Override
    public boolean init(CommandSender sender, String[] args) {
        if (args.length>0) {
            try {
                type = Type.valueOf(args[0]);
            } catch (IllegalArgumentException ie) {
                error(sender, "Unknown type: " + args[0]);
                return false;
            }

            if (args.length>1) {
                try {
                    display = Display.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    error(sender, "Unknown display arg: " + args[1]);
                    return false;
                }
            }

        }

        if (display==Display.replace && inputs.length<2) {
            error(sender, "Expecting at least 2 inputs. 1 clock pin and 1 data pin.");
            return false;
        } else if (display==Display.add && inputs.length<3) {
            error(sender, "Expecting at least 3 inputs. 1 clock pin, 1 clear pin and 1 data pin.");
            return false;
        } else if (display==Display.scroll && inputs.length<4) {
            error(sender, "Expecting at least 4 inputs. 1 clock pin, 1 clear pin, 1 scroll pin and 1 data pin.");
            return false;
        }

        if (interfaceBlocks.length==0) {
            error(sender, "Expecting at least 1 interface block.");
            return false;
        }

        List<Location> str = new ArrayList<Location>();
        List<Location> signs = new ArrayList<Location>();
        str.addAll(Arrays.asList(this.structure));

        for (InterfaceBlock ib : interfaceBlocks) {
            Location loc = ib.getLocation();
            Location north = Locations.getFace(loc, BlockFace.NORTH);
            Location south = Locations.getFace(loc, BlockFace.SOUTH);
            Location west = Locations.getFace(loc, BlockFace.WEST);
            Location east = Locations.getFace(loc, BlockFace.EAST);
            Location up = Locations.getFace(loc, BlockFace.UP);

            Block i = loc.getBlock();
            if (checkBlock(i, north)) { str.add(north); signs.add(north); }
            if (checkBlock(i, south)) { str.add(south); signs.add(south); }
            if (checkBlock(i, west)) { str.add(west); signs.add(west); }
            if (checkBlock(i, east)) { str.add(east); signs.add(east); }
            if (checkBlock(i, up)) { str.add(up); signs.add(up); }
        }

        if (signs.isEmpty()) {
            error(sender, "Couldn't find any signs attached to the chip's interface blocks.");
            return false;
        } else {
            this.structure = str.toArray(new Location[str.size()]);
            info(sender, "Found " + signs.size() + " sign(s) to print on.");
        }

        signList = signs.toArray(new Location[signs.size()]);
        //signUpdateTask = new SignUpdateTask(signs.toArray(new Location[signs.size()]));

        if (display==Display.replace) dataPin = 1;
        else if (display==Display.add) dataPin = 2;
        else if (display==Display.scroll) dataPin = 3;

        redstoneChips.registerRcTypeReceiver(activationBlock, this);

        return true;
    }

    private boolean checkBlock(Block i, Location s) {
        // TODO: Check whether this method loads the chunk or not.
        Block sign = s.getBlock();
        MaterialData data = sign.getState().getData();
        if (data instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
            if (sign.getRelative(signData.getAttachedFace()).equals(i)) // make sure the sign is actually attached to the interface block.
                return true;
            else return false;

        } else return false;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        Object text = state.get("text");

        if (text!=null) {
            String t = text.toString();
            textBuffer.setLength(0);
            textBuffer.append(t);
        }
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String,String>();
        state.put("text", textBuffer.toString());
        return state;
    }


    class SignUpdateTask implements Runnable {
        int curSign = 0;

        @Override
        public void run() {
            Sign s = (Sign)signList[curSign].getBlock().getState();
            s.setLine(0, lines[0]);
            s.setLine(1, lines[1]);
            s.setLine(2, lines[2]);
            s.setLine(3, lines[3]);
            s.update();
            
            if (curSign<signList.length-1) {
                curSign++;

                redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, signUpdateTask, 1);
            } else curSign=0;

        }
    }

    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) return;

        String text = "";
        for (String word : words)
            text += word + " ";
        updateText(text.substring(0, text.length()-1));
        updateSigns();
    }


}
