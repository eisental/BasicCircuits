package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;



/**
 *
 * @author Tal Eisenberg
 */
public class print extends Circuit {
    boolean firstUpdate = true;
    private final static int clockPin = 0;

    enum Type {
        num, signed, unsigned, ascii, hex, oct, bin;
    }

    Type type = Type.num;
    String[] lines = new String[4];
    String curText = "";
    Block[] blocksToUpdate;

    boolean add;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==clockPin && newLevel) {
            updateText();
            updateSigns();
        }
    }

    private void updateText() {
        String text = "";

        if (type==Type.num || type==Type.unsigned) {
            text = Integer.toString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
        } else if (type==Type.signed) {
            text = Integer.toString(Circuit.bitSetToSignedInt(inputBits, 1, inputs.length-1));
        } else if (type==Type.hex) {
            text = Integer.toHexString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
        } else if (type==Type.oct) {
            text = Integer.toOctalString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
        } else if (type==Type.bin) {
            for (int i=1; i<inputs.length; i++) text += (inputBits.get(i)?"1":"0");
        } else if (type==Type.ascii) {
            text = "" + (char)Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
        } else text = "err";

        if (add) {
            if (type==Type.bin || type==Type.ascii || curText.length()==0) {
                curText = curText + text;
            } else
                curText = curText + " " + text;
        } else curText = text;

        if (curText.length()>36) {
            lines[0] = curText.substring(0, 12);
            lines[1] = curText.substring(12, 24);
            lines[2] = curText.substring(24, 36);
            lines[3] = curText.substring(36, 48);
        } else if (curText.length()>24) {
            lines[0] = "";
            lines[1] = curText.substring(0, 12);
            lines[2] = curText.substring(12, 24);
            lines[3] = curText.substring(24);
        } else if (curText.length()>12) {
            lines[0] = "";
            lines[1] = curText.substring(0,12);
            lines[2] = curText.substring(12);
            lines[3] = "";
        } else {
            lines[0] = "";
            lines[1] = curText;
            lines[2] = "";
            lines[3] = "";
        }

        if (hasDebuggers()) {
            debug("printing:");
            debug(lines[0]);
            debug(lines[1]);
            debug(lines[2]);
            debug(lines[3]);
        }

        if (curText.length()>48) curText = "";
    }

    private void updateSigns() {
        for (Block b : blocksToUpdate) {
            if (b.getType()==Material.WALL_SIGN) {
                Sign sign = (Sign)b.getState();
                sign.setLine(0, lines[0]);
                sign.setLine(1, lines[1]);
                sign.setLine(2, lines[2]);
                sign.setLine(3, lines[3]);
            }
        }
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (inputs.length<2) {
            error(player, "Expecting at least 2 inputs. Input 0 is clock input.");
            return false;
        }

        if (args.length>0) {
            Type arg = null;
            for (Type t : Type.values()) {
                if (t.name().equals(args[0])) {
                    arg = t;
                    break;
                }
            }


            if (args.length>1) {
                if (args[1].equals("add"))
                    add = true;
            }

            if (arg==null) {
                error(player, "Unknown type: " + args[0]);
                return false;
            } else type = arg;
        }

        if (interfaceBlocks.length==0) {
            error(player, "Expecting at least 1 interaction block.");
            return false;
        }

        List<Block> blockList = new ArrayList<Block>();
        for (Block i : interfaceBlocks) {
            Block north = i.getFace(BlockFace.NORTH);
            Block south = i.getFace(BlockFace.SOUTH);
            Block west = i.getFace(BlockFace.WEST);
            Block east = i.getFace(BlockFace.EAST);
            if (!isStructureBlock(north)) blockList.add(north);
            if (!isStructureBlock(south)) blockList.add(south);
            if (!isStructureBlock(west)) blockList.add(west);
            if (!isStructureBlock(east)) blockList.add(east);
        }

        this.blocksToUpdate = blockList.toArray(new Block[blockList.size()]);

        return true;
    }

    private boolean isStructureBlock(Block b) {
        for (Block s : structure)
            if (b==s) return true;

        return false;
    }
}
