package org.tal.basiccircuits;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class ipreceiver extends Circuit {

    DatagramSocket socket;
    List<InetAddress> authorizedAddresses;
    byte[] buf;
    boolean closeConnection = false;
    DatagramPacket packet;
    private final static int SO_TIMEOUT = 20; // receive timeout in milliseconds. Could have been also 1ms i guess.

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (high) {
            try {
                socket.receive(packet);
                if (authorizedAddresses.contains(packet.getAddress())) {
                    // update data outputs according to the packet.
                    byte[] bytes = packet.getData();
                    BitSet7 bits = BitSet7.valueOf(bytes);
                    sendBitSet(1, outputs.length-1, bits);
                    
                    if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, outputs.length-1) + " from " + packet.getAddress() + ":" + packet.getPort());
                    
                    // pulse the output clock pin.
                    sendOutput(0, true);
                    sendOutput(0, false);

                } else if (hasDebuggers())
                    debug("Received data from unauthorized address " + packet.getAddress() + ".");
            } catch (SocketTimeoutException sx) {
                if (hasDebuggers()) debug("No data is available.");
            } catch (IOException ex) {
                Logger.getLogger(ipreceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (outputs.length<2) {
            error(sender, "Expecting at least 2 outputs. 1 output clock pin and 1 or more data pins.");
            return false;
        } if (inputs.length!=1) {
            error(sender, "Expecting 1 clock input.");
            return false;
        }

        if (args.length<2) {
            error(sender, "Expecting a port sign argument and at least one authorized incoming address.");
            return false;
        }

        try {
            int port = Integer.decode(args[0]);
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(SO_TIMEOUT);
                info(sender, "Listening on port " + socket.getLocalPort());
            } catch (BindException be) {
                error(sender, "Port " + port + " is already used.");
                return false;
            } catch (SocketException ex) {
                error(sender, "Socket exception: " + ex);
            }
        } catch (NumberFormatException ne) {
            error(sender, "Bad port number: " + args[1]);
            return false;
        }

        // incoming addresses
        authorizedAddresses = new ArrayList<InetAddress>();
        
        for (int i=1; i<args.length; i++) {
            try {
                authorizedAddresses.add(InetAddress.getByName(args[i]));
            } catch (UnknownHostException ex) {
                error(sender, "Unknown host: " + args[i]);
                return false;
            }
        }

        buf = new byte[(int)Math.ceil((outputs.length-1)/8d)];
        packet = new DatagramPacket(buf, buf.length);

        return true;

    }

    @Override
    public void circuitDestroyed() {
        if (socket!=null) socket.close();
    }
}
