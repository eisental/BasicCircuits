package org.tal.basiccircuits;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

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
    protected boolean init(Player player, String[] args) {
        if (args.length!=2) {
            error(player, "Expecting 2 sign arguments. remote address, and port.");
            return false;
        }

        try {
            address = InetAddress.getByName(args[0]);
            port = Integer.decode(args[1]);
            try {
                socket = new DatagramSocket();
            } catch (SocketException ex) {
                Logger.getLogger(iptransmitter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NumberFormatException ne) {
            error(player, "Bad port number: " + args[1]);
            return false;
        } catch (UnknownHostException ue) {
            error(player, "Unknown host: " + args[0]);
            return false;
        }

        return true;
    }

    private void udpBitSet(BitSet7 bits, int startIdx, int length) {
        BitSet7 out = bits.get(startIdx, length+startIdx);
        byte[] buf = out.toByteArray(); 
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            if (hasDebuggers()) debug("Sending " + Circuit.bitSetToBinaryString(out, 0, length));
            socket.send(packet);
        } catch (IOException ie) {
            error(null, "iptransmitter: " + ie);
        }
    }

    @Override
    public void circuitDestroyed() {
        socket.close();
    }
}
