package org.tal.basiccircuits;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;
import net.eisental.common.parsing.Range;

/**
 *
 * @author Tal Eisenberg
 */
public class iptransmitter extends Circuit {
    private DatagramSocket socket = null;
    private InetAddress address;
    private int port;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inputs.length==1) {
            udpBitSet(inputBits, 0, inputs.length);
        } else {
            if (inIdx==0 && high) { // clock pin
                udpBitSet(inputBits, 1, inputs.length-1);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (args.length!=2) {
            error(sender, "Expecting 2 sign arguments. remote address, and port.");
            return false;
        }

        try {
            address = InetAddress.getByName(args[0]);
            port = Integer.decode(args[1]);
            Object oRange = redstoneChips.getPrefs().getPrefs().get("iptransmitter.ports");
            if (oRange==null) {
                error(sender, "No ports are allowed. Please set a port range by changing the iptransmitter.ports preferences key.");
                return false;
            }

            String sRange = oRange.toString();
            Range portRange = new Range(sRange, Range.Type.OPEN_ALLOWED);
            if (!portRange.isInRange(port)) {
                error(sender, "Port " + port + " is not allowed. Use ports in the range of " + portRange.toString());
                error(sender, "You can change the port range by changing the iptransmitter.ports preferences key.");
                return false;
            }

            try {
                socket = new DatagramSocket();
            } catch (SocketException ex) {
                Logger.getLogger(iptransmitter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NumberFormatException ne) {
            error(sender, "Bad port number: " + args[1]);
            return false;
        } catch (UnknownHostException ue) {
            error(sender, "Unknown host: " + args[0]);
            return false;
        }

        return true;
    }

    private void udpBitSet(BitSet7 bits, int startIdx, int length) {
        BitSet7 out = bits.get(startIdx, length+startIdx);
        byte[] buf = out.toByteArray(); 
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            if (hasDebuggers()) debug("Sending " + BitSetUtils.bitSetToBinaryString(out, 0, length));
            socket.send(packet);
        } catch (IOException ie) {
            error(null, "iptransmitter: " + ie);
        }
    }

    @Override
    public void circuitShutdown() {
        socket.close();
    }
}
