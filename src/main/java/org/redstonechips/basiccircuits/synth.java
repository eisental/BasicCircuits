package org.redstonechips.basiccircuits;

//import java.lang.instrument.Instrumentation;
//import java.util.Collection;
import java.util.regex.Pattern;

//import javax.sound.midi.Instrument;

//import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Instrument;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
//import org.redstonechips.user.UserSession;
import org.redstonechips.chip.io.InterfaceBlock;
//import org.bukkit.block.NoteBlock;
//import org.bukkit.block.data.type.NoteBlock;
//import org.bukkit.block.BlockState;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;
import org.redstonechips.wireless.Receiver;

/**
 *
 * @author Tal Eisenberg
 */
public class synth extends Circuit {
    private boolean indexedPitch = false;
    private byte[] pitchIndex;
    boolean instrumentPins = false;

    public static final Instrument[] instrument = new Instrument[] {
    	Instrument.BANJO,
    	Instrument.BASS_DRUM,
    	Instrument.BASS_GUITAR,
    	Instrument.BELL,
    	Instrument.BIT,
    	Instrument.CHIME,
    	Instrument.COW_BELL,
    	Instrument.DIDGERIDOO,
    	Instrument.FLUTE,
    	Instrument.GUITAR,
    	Instrument.IRON_XYLOPHONE,
    	Instrument.PIANO,
    	Instrument.PLING,
    	Instrument.SNARE_DRUM,
    	Instrument.STICKS,
    	Instrument.XYLOPHONE,
    };
    public Instrument instruSel (String instruname){
    	switch(instruname.toUpperCase()) {
    	case "BANJO":
    		return(instrument[0]);
    	case "BASS DRUM":
    		return(instrument[1]);
    	case "BASS GUITAR":
    		return(instrument[2]);
    	case "BELL":
    		return(instrument[3]);
    	case "BIT":
    		return(instrument[4]);
    	case "CHIME":
    		return(instrument[5]);
    	case "COW BELL":
    		return(instrument[6]);
    	case "DIDGERIDOO":
    		return(instrument[7]);
    	case "FLUTE":
    		return(instrument[8]);
    	case "GUITAR":
    		return(instrument[9]);
    	case "IRON XYLOPHONE":
    		return(instrument[10]);
    	case "PIANO":
    		return(instrument[11]);
    	case "PLING":
    		return(instrument[12]);
    	case "SNARE DRUM":
    		return(instrument[13]);
    	case "STICKS":
    		return(instrument[14]);
    	case "XYLOPHONE":
    		return(instrument[15]);
    	}
    	return(null);
    }
    private Receiver receiver;

    Instrument inst =  instrument[11]; // set piano as default instrument

    public static final Pattern MIDINOTE_PATTERN = Pattern.compile("[a-gA-G][#b]?\\-?[0-8]+");


    @Override
    public void input(boolean state, int inIdx) {
        int val;


        if (!instrumentPins && state) {
        	if (inputlen==1) {
        		val = (int)BooleanArrays.toUnsignedInt(inputs, 0, 1)-1;
        	} else if (inIdx==0) { // clock pin
        		val = (int)BooleanArrays.toUnsignedInt(inputs, 1, inputlen-1);
        	}
        	else return;
        	playNote2(val);
        }
        if (instrumentPins && state) {
        	if (inIdx==0) { // clock pin
        		val = (int)BooleanArrays.toUnsignedInt(inputs, 1, inputlen-5);
        		inst = instrument[((int)BooleanArrays.toUnsignedInt(inputs, inputlen-4, 4))];
        	}
        	else return;
        	playNote2(val);
        }
    }

    class SynthReceiver extends Receiver {
        @Override
        public void receive(BooleanSubset bits) {
            if (!instrumentPins) playNote2((int)bits.toUnsignedInt());
            else {
            	inst = instrument[(int)bits.toUnsignedInt(bits.length()-4, 4)];
            	playNote2((int)bits.toUnsignedInt(0, bits.length()-4));
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        // needs to have 5 inputs 1 clock 4 data
        String channel = null;
        boolean instrumentarg = false;

        inst = instrument[11];

        if (args.length>0) {
            if (args[args.length-1].startsWith("#")) { // channel arg
                channel = args[args.length-1].substring(1);
            }
            for (byte i=0; i<args.length; i++) {
            	if (args[i].startsWith("$")) { // channel arg

            		if (args[i].toUpperCase().substring(args[i].indexOf("$")+1).equals("INSTRUMENT")) {
            			instrumentPins = true;
            		}
            		else {
            			inst = instruSel(args[i].substring(args[i].indexOf("$")+1));
            			if (inst==null) return error("Not a valid instrument");
            		}

                instrumentarg = true;
            	}
            }
            if (!instrumentarg) {
                if (args.length>=(channel!=null?2:1)) {
                   indexedPitch = true;
                   pitchIndex = new byte[(channel==null?args.length:args.length-1)];
                   for (int i=0; i<pitchIndex.length; i++) {
                       try {
                           pitchIndex[i] = (byte)noteStringToData(args[i]);
                        } catch (IllegalArgumentException ie) {
                           return error(ie.getMessage());
                        }
                   }
               }
           }
            else {
            	if (args.length>=(channel!=null?3:2)) {
                	indexedPitch = true;
                    pitchIndex = new byte[(channel==null?args.length-1:args.length-2)];
                    for (int i=0; i<pitchIndex.length; i++) {
                        try {
                            pitchIndex[i] = (byte)noteStringToData(args[i]);
                         } catch (IllegalArgumentException ie) {
                        return error(ie.getMessage());
                         }
                    }
            	}
            }
        }

        if (chip.interfaceBlocks.length==0) return error("Expecting at least 1 interface block.");
        if (inputlen!=0) {

        	if (indexedPitch && instrumentPins) {
        		if (inputlen<(5+Math.ceil(Math.log(pitchIndex.length)/Math.log(2)))) {
        			if (channel!=null) return error("Not enough inputs. Indexed mode with instrument selection requires 1 clock pin, 4 instrument pins and " + (int)Math.ceil(Math.log(pitchIndex.length)/Math.log(2)) + " pitch index pins.  You have a channel listed, this mode will run with 0 inputs");
        			else return error("Not enough inputs. Indexed mode with instrument selection requires 1 clock pin, 4 instrument pins and " + (int)Math.ceil(Math.log(pitchIndex.length)/Math.log(2)) + " pitch index pins");
        		}
        		if (inputlen>(5+Math.ceil(Math.log(pitchIndex.length)/Math.log(2)))) {
        			return error("Too many inputs. Indexed mode with instrument selection requires 1 clock pin, 4 instrument pins and " + (int)Math.ceil(Math.log(pitchIndex.length)/Math.log(2)) + " pitch index pins");
        		}
        	}

        else if (inputlen>6 && !indexedPitch && !instrumentPins)
            return error("Too many inputs. Direct mode requires 1 clock pin and no more than 5 data pins.");
        else if (inputlen>10 && !indexedPitch && instrumentPins)
        	return error("Too many inputs. Direct mode with instrument selection requires 1 clock pin and no more than 9 data pins.");
        else if (inputlen<6 && !indexedPitch && instrumentPins) {
        	if (channel!=null) return error("Not enough inputs. Expecting 1 clock pin and at 4 instrument pins. You have a channel listed, this mode will run with 0 inputs");
        	else return error("Not enough inputs. Expecting 1 clock pin and at 4 instrument pins.");
        	}

        }
        if (channel==null && inputlen==0) { // if no channel and no clock pin
            return error("Expecting at least 1 input pin.");
        }
        if (channel!=null) {
            int len;
            if (indexedPitch && !instrumentPins) len = (int)Math.ceil(Math.log(pitchIndex.length)/Math.log(2));
            else if (indexedPitch && instrumentPins) len = (int)Math.ceil(Math.log(pitchIndex.length)/Math.log(2) + 4);
            else if (instrumentPins) len = 9;
            else len = 5;

            try {
                receiver = new SynthReceiver();
                receiver.init(activator, channel, len, this);
            } catch (IllegalArgumentException e) {
                return error(e.getMessage());
            }

        }

        return this;
    }

    private void playNote2(int val) {
        byte pitch;

        if (indexedPitch) {
            int index = val;
            if (index>=pitchIndex.length) {
                if (chip.hasListeners()) debug("pitch index out of bounds: " + index);
                return;
            }
            pitch = pitchIndex[index];
        } else {
            if (val>24) {
                if (chip.hasListeners()) debug("pitch value is too high: " + val);
                return;
            }
            pitch = (byte)val;
        }

        if (pitch==-1) { // rest
            if (chip.hasListeners()) debug("Setting note blocks to rest");
        } else {
            if (chip.hasListeners()) debug("Setting note blocks pitch to " + dataToNoteString(pitch) + " (" + pitch + ")");
            for (InterfaceBlock i : chip.interfaceBlocks) {
                Block block = i.getLocation().getBlock();
                tryToPlay(block.getRelative(BlockFace.NORTH), pitch);
                tryToPlay(block.getRelative(BlockFace.SOUTH), pitch);
                tryToPlay(block.getRelative(BlockFace.WEST), pitch);
                tryToPlay(block.getRelative(BlockFace.EAST), pitch);
                tryToPlay(block.getRelative(BlockFace.UP), pitch);
                tryToPlay(block.getRelative(BlockFace.DOWN), pitch);
            }
        }

    }

    private void tryToPlay(Block block, byte pitch) {
    	if (block.getType()==Material.NOTE_BLOCK) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
        	   player.playNote(block.getLocation(), inst, new Note(pitch));
            }
        }
    }

    private int noteStringToData(String note) {
        if (note.equalsIgnoreCase("r"))
            return -1;

        // possible inputs: 0...127 / X[#]0..10
        int keynum;
        if (note.matches("[\\-0-9]+")) { // the whole string is a number
            keynum = Integer.parseInt(note);
        } else if (MIDINOTE_PATTERN.matcher(note).matches()) {
            int octave = Integer.parseInt(note.split("[a-gA-G][#b]?")[1])-1;
            String key = note.split("\\-?[0-8]+")[0];
            if (key.substring(0,1).matches("[a-gA-G]")) {
                if (key.charAt(0)=='c') keynum = 0;
                else if (key.charAt(0)=='d') keynum = 2;
                else if (key.charAt(0)=='e') keynum = 4;
                else if (key.charAt(0)=='f') keynum = 5;
                else if (key.charAt(0)=='g') keynum = 7;
                else if (key.charAt(0)=='a') keynum = 9;
                else if (key.charAt(0)=='b') keynum = 11;
                else throw new IllegalArgumentException("Bad note name " + note);
                if (key.length()>1) {
                    if (key.charAt(1)=='#') keynum++;
                    else if (key.charAt(1)=='b') keynum--;
                }
                keynum = (keynum-6) + (octave)*12; // MIDI to minecraft
                if (keynum>24 || keynum<0) throw new IllegalArgumentException(note + " is out of bounds. (" + keynum + "). The pitch range should be f#1 to f#3 or 0 to 24.");
            } else throw new IllegalArgumentException("Bad note name: " + note);
        } else throw new IllegalArgumentException("Bad note name: " + note);
        return keynum;
    }

    private String dataToNoteString(int note) {
        if (note==-1) return "r";

        // 0 = f#1,
        note += 6;
        int keynum, octave;
        keynum = note % 12;
        octave = (note-keynum)/12+1; // octave 1 is the first.
        if (keynum==0) return "c" + octave;
        else if (keynum==1) return "c#" + octave;
        else if (keynum==2) return "d" + octave;
        else if (keynum==3) return "d#" + octave;
        else if (keynum==4) return "e" + octave;
        else if (keynum==5) return "f" + octave;
        else if (keynum==6) return "f#" + octave;
        else if (keynum==7) return "g" + octave;
        else if (keynum==8) return "g#" + octave;
        else if (keynum==9) return "a" + octave;
        else if (keynum==10) return "a#" + octave;
        else if (keynum==11) return "b" + octave;
        else throw new IllegalArgumentException();
    }
}
