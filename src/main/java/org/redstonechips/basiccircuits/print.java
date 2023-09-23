package org.redstonechips.basiccircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCTypeReceiver;
import org.redstonechips.basiccircuits.SignWriter.DisplayMode;
import org.redstonechips.basiccircuits.SignWriter.Type;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.wireless.Receiver;


/**
 *
 * @author Tal Eisenberg
 */
public class print extends Circuit implements RCTypeReceiver {
    private final static int clockPin = 0;
    private final static int scrollPin = 2;
    private final static int clearPin = 1;

    private int dataPin = 1;
    private SignWriter writer;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==clockPin) {
            if (state) write(inputs, dataPin, inputlen-dataPin);

        } else if (inIdx==clearPin && (writer.getDisplayMode()==DisplayMode.scroll ||
                writer.getDisplayMode()==DisplayMode.add)) {
            if (state) clear();

        } else if (inIdx==scrollPin && writer.getDisplayMode()==DisplayMode.scroll) {
            if (state) writer.scroll(1);

        } else if (inputs[clockPin])
            write(inputs, dataPin, inputlen-dataPin);

    }

    class WriteReceiver extends Receiver {

        @Override
        public void receive(BooleanSubset bits) {
            write(bits.copy(), 0, getLength());
        }
    }

    class ClearReceiver extends Receiver {

        @Override
        public void receive(BooleanSubset bits) {
            if (bits.get(0)) writer.clear();
        }
    }

    class ScrollReceiver extends Receiver {
        @Override
        public void receive(BooleanSubset bits) {
            if (bits.get(0)) writer.scroll(1);
        }

    }

    private void clear() {
        if (chip.hasListeners()) debug("Clearing signs.");
        writer.clear();
    }

    private void write(boolean[] bits, int start, int length) {
        writer.write(bits, start, length);
        if (chip.hasListeners()) {
            String[] lines = writer.getLines();
            debug("text:");
            debug(lines[0]);
            debug(lines[1]);
            debug(lines[2]);
            debug(lines[3]);
        }
    }

    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) return;

        String text = "";
        for (String word : words)
            text += word + " ";
        writer.write(text.substring(0, text.length()-1));
    }

    @Override
    public Circuit init(String[] args) {
        String channel = null;
        Type type = Type.num;
        DisplayMode display = DisplayMode.replace;

        if (args.length>0) {
            if (args[args.length-1].startsWith("#")) { // channel arg
                channel = args[args.length-1].substring(1);
            }

            if (args.length>=(channel!=null?2:1)) {
                try {
                    type = Type.valueOf(args[0]);
                } catch (IllegalArgumentException ie) {
                    return error("Unknown type: " + args[0]);
                }
            }

            if (args.length>=(channel!=null?3:2)) {
                try {
                    display = DisplayMode.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    return error("Unknown display arg: " + args[1]);
                }
            }
        }

        if (channel==null) {
            if (display==DisplayMode.replace && inputlen<2) {
                return error("Expecting at least 2 inputs. 1 clock pin and 1 data pin.");
            } else if (display==DisplayMode.add && inputlen<3) {
                return error("Expecting at least 3 inputs. 1 clock pin, 1 clear pin and 1 data pin.");
            } else if (display==DisplayMode.scroll && inputlen<4) {
                return error("Expecting at least 4 inputs. 1 clock pin, 1 clear pin, 1 scroll pin and 1 data pin.");
            }
        }

        if (chip.interfaceBlocks.length==0) return error("Expecting at least 1 interface block.");

        if (display==DisplayMode.replace) dataPin = 1;
        else if (display==DisplayMode.add) dataPin = 2;
        else if (display==DisplayMode.scroll) dataPin = 3;

        writer = SignWriter.getSignWriter(display, type, IOBlock.locationsOf(chip.interfaceBlocks));
        if (writer.getSigns().isEmpty()) {
            return error("Couldn't find any signs attached to the chip interface blocks.");
        } else {
            List<Location> str = new ArrayList<>();
            str.addAll(Arrays.asList(chip.structure));
            str.addAll(writer.getSigns());
            chip.structure = str.toArray(new Location[str.size()]);
            info("Found " + writer.getSigns().size() + " sign(s) to print on.");
        }


        rc.addRCTypeReceiver(chip.activationBlock, this);

        if (channel!=null && !initReceiver(activator, channel, type)) return null;
        else return this;
    }

    private boolean initReceiver(CommandSender sender, String channel, Type type) {
        try {
            Receiver writeReceiver = new WriteReceiver();
            writeReceiver.init(sender, channel, type==Type.ascii?8:32, this);

            if (writer.getDisplayMode()==DisplayMode.add) {
                Receiver clearReceiver = new ClearReceiver();
                clearReceiver.init(sender, channel, 1, this);

                writeReceiver.setStartBit(writeReceiver.getStartBit()+1);
            } else if (writer.getDisplayMode()==DisplayMode.scroll) {
                Receiver clearReceiver = new ClearReceiver();
                clearReceiver.init(sender, channel, 1, this);

                Receiver scrollReceiver = new ScrollReceiver();
                scrollReceiver.init(sender, channel, 1, this);
                scrollReceiver.setStartBit(scrollReceiver.getStartBit()+1);

                writeReceiver.setStartBit(writeReceiver.getStartBit()+2);

            }

            return true;
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            return false;
        }
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        Object text = state.get("text");

        if (text!=null)
            writer.setText(text.toString());
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<>();
        state.put("text", writer.getText());
        return state;
    }

    @Override
    public boolean isStateless() {
        return false;
    }
}
