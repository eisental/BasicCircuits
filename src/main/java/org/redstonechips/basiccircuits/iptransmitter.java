package org.redstonechips.basiccircuits;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redstonechips.chip.Circuit;
import org.redstonechips.parsing.Range;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class iptransmitter extends Circuit {
    private DatagramSocket socket = null;
    private InetAddress address;
    private int port;

    @Override
    public void input(boolean state, int inIdx) {
        if (inputlen==1) {
            udpBits(inputs, 0, inputlen);
        } else {
            if (inIdx==0 && state) { // clock pin
                udpBits(inputs, 1, inputlen-1);
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (args.length!=2) return error("Expecting 2 sign arguments. remote address, and port.");

        try {
            address = InetAddress.getByName(args[0]);
            port = Integer.decode(args[1]);
            Object oRange = rc.prefs().getPref("iptransmitter.ports");
            if (oRange==null) 
                return error("No ports are allowed. Please set a port range by changing the iptransmitter.ports preferences key.");

            String sRange = oRange.toString();
            Range portRange = new Range(sRange, Range.Type.OPEN_ALLOWED);
            if (!portRange.isInRange(port)) {
                return error("Port " + port + " is not allowed. Use ports in the range of " + portRange.toString() 
                        + ". You can change the port range by changing the iptransmitter.ports preferences key.");
            }

            try {
                socket = new DatagramSocket();
            } catch (SocketException ex) {
                Logger.getLogger(iptransmitter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NumberFormatException ne) {
            return error("Bad port number: " + args[1]);
        } catch (UnknownHostException ue) {
            return error("Unknown host: " + args[0]);
        }

        return this;
    }

    private void udpBits(boolean[] bits, int start, int length) {
        BitSet out = new BitSet(length);
        for (int i=0; i<length; i++) out.set(i, bits[start+i]);        
        byte[] buf = out.toByteArray(); 
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            if (chip.hasListeners()) debug("Sending " + BooleanArrays.toPrettyString(bits, start, length));
            socket.send(packet);
        } catch (IOException ie) {
            error("iptransmitter: " + ie);
        }
    }

    @Override
    public void shutdown() {
        socket.close();
    }
}
