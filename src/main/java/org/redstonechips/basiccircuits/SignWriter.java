package org.redstonechips.basiccircuits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.Locations;

/**
 *
 * @author Tal Eisenberg
 */
public class SignWriter {
    public enum Type {
        num, signed, unsigned, ascii, hex, oct, bin;
    }

    public enum DisplayMode {
        replace, add, scroll
    }

    public static final int LineWidth = 15;

    private final Type type;
    private final DisplayMode display;

    private final StringBuffer textBuffer = new StringBuffer();

    private String[] lines = new String[] { "", "", "", "" };
    private int scrollPos = 0;
    private final List<Location> signList;

    public SignWriter(DisplayMode displayMode, Type type, List<Location> signList) {
        this.type = type;
        this.display = displayMode;
        this.signList = signList;
    }

    public DisplayMode getDisplayMode() {
        return display;
    }

    public String[] getLines() { return lines; }

    public void setLines(String[] lines) {
        this.lines = lines;
    }

    public String getText() {
        return textBuffer.toString();
    }

    public void setText(String text) {
        textBuffer.setLength(0);
        textBuffer.append(text.toString());

        if (display==DisplayMode.scroll)
            prepScrollLines();
        else prepWrapLines();
        updateSigns();
    }

    List<Location> getSigns() { return signList; }

    public void write(String text) {
        if (display==DisplayMode.add) {
            add(text);
            prepWrapLines();
            if (textBuffer.length()>LineWidth*4) textBuffer.setLength(0);
        } else if (display==DisplayMode.replace) {
            textBuffer.setLength(0);
            textBuffer.append(text);
            prepWrapLines();
        } else if (display==DisplayMode.scroll) {
            add(text);
            prepScrollLines();
        }

        updateSigns();
    }

    public void write(boolean[] bits, int start, int length) {
        write(convertBits(bits, start, length));
    }

    public void clear() {
        textBuffer.setLength(0);
        scrollPos = 0;
        lines[0] = "";
        lines[1] = "";
        lines[2] = "";
        lines[3] = "";
        updateSigns();
    }

    public void scroll(int amount) {
        if (scrollPos>=textBuffer.length()-1)
            scrollPos = 0;
        else
            scrollPos += amount;

        prepScrollLines();

        updateSigns();
    }

    private void add(String text) {
        if (type==Type.ascii || textBuffer.length()==0) {
            textBuffer.append(text);
        } else
            textBuffer.append(" ").append(text);
    }

    private void updateSigns() {
        for (Location l : signList) {
            Sign s = (Sign)l.getBlock().getState();
            s.setLine(0, lines[0]);
            s.setLine(1, lines[1]);
            s.setLine(2, lines[2]);
            s.setLine(3, lines[3]);
            s.update();
        }
    }

    private void prepScrollLines() {
        String window;

        if (textBuffer.length()>LineWidth) { // turn scrolling on
            int end = Math.min(scrollPos+LineWidth, textBuffer.length());
            window = textBuffer.substring(scrollPos, end);
            if (window.length()<LineWidth) {
                window += " " + textBuffer.substring(0, LineWidth-window.length());
            }
        } else window = textBuffer.toString();

        lines[0] = "";
        lines[1] = window;
        lines[2] = "";
        lines[3] = "";
    }

    private void prepWrapLines() {
        if (textBuffer.length()>LineWidth*3) {
            String line4 = textBuffer.substring(LineWidth*3);
            if (line4.length()>LineWidth) line4 = line4.substring(0, LineWidth);

            lines[0] = textBuffer.substring(0, LineWidth);
            lines[1] = textBuffer.substring(LineWidth, LineWidth*2);
            lines[2] = textBuffer.substring(LineWidth*2, LineWidth*3);
            lines[3] = line4;
        } else if (textBuffer.length()>LineWidth*2) {
            lines[0] = textBuffer.substring(0, LineWidth);
            lines[1] = textBuffer.substring(LineWidth, LineWidth*2);
            lines[2] = textBuffer.substring(LineWidth*2);
            lines[3] = "";
        } else if (textBuffer.length()>LineWidth) {
            lines[0] = textBuffer.substring(0,LineWidth);
            lines[1] = textBuffer.substring(LineWidth);
            lines[2] = "";
            lines[3] = "";
        } else {
            lines[0] = textBuffer.toString();
            lines[1] = "";
            lines[2] = "";
            lines[3] = "";
        }
    }

    private String convertBits(boolean[] bits, int start, int length) {
        String text = null;

        if (type==Type.num || type==Type.unsigned) {
            text = Long.toString(BooleanArrays.toUnsignedInt(bits, start, length));
        } else if (type==Type.signed) {
            text = Long.toString(BooleanArrays.toSignedInt(bits, start, length));
        } else if (type==Type.hex) {
            text = Long.toHexString(BooleanArrays.toUnsignedInt(bits, start, length));
        } else if (type==Type.oct) {
            text = Long.toOctalString(BooleanArrays.toUnsignedInt(bits, start, length));
        } else if (type==Type.bin) {
            text = BooleanArrays.toString(bits, start, (bits.length==0?1:bits.length));
        } else if (type==Type.ascii) {
            char c = (char)BooleanArrays.toUnsignedInt(bits, start, length);
            if (!Character.isISOControl(c)) text = Character.toString(c);
            else text = "";
        }

        return text;
    }

    public static SignWriter getSignWriter(DisplayMode mode, Type type, Location... aroundBlocks) {
        List<Location> signs = new ArrayList<>();

        for (Location loc : aroundBlocks) {
            Location north = Locations.getFace(loc, BlockFace.NORTH);
            Location south = Locations.getFace(loc, BlockFace.SOUTH);
            Location west = Locations.getFace(loc, BlockFace.WEST);
            Location east = Locations.getFace(loc, BlockFace.EAST);
            Location up = Locations.getFace(loc, BlockFace.UP);

            Block i = loc.getBlock();
            if (checkBlock(i, north)) { signs.add(north); }
            if (checkBlock(i, south)) { signs.add(south); }
            if (checkBlock(i, west)) { signs.add(west); }
            if (checkBlock(i, east)) { signs.add(east); }
            if (checkBlock(i, up)) { signs.add(up); }
        }

        return new SignWriter(mode, type, signs);
    }

    private static boolean checkBlock(Block i, Location s) {
        // TODO: Check whether this method loads the chunk or not.
        Block sign = s.getBlock();
        MaterialData data = sign.getState().getData();
        if (data instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
            return sign.getRelative(signData.getAttachedFace()).equals(i);

        } else return false;
    }

}
