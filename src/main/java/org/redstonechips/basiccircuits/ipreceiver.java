package org.redstonechips.basiccircuits;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BitSetUtils;

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
    public void input(boolean state, int inIdx) {
        if (state) {
            try {
                socket.receive(packet);
                if (authorizedAddresses==null || authorizedAddresses.contains(packet.getAddress())) {
                    // update data outputs according to the packet.
                    byte[] bytes = packet.getData();
                    BitSet bits = BitSet.valueOf(bytes);
                    writeBitSet(bits, 1, outputlen-1);

                    if (chip.hasListeners()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, outputlen-1) + " from " + packet.getAddress() + ":" + packet.getPort());

                    // pulse the output clock pin.
                    write(true, 0);
                    write(false, 0);

                } else if (chip.hasListeners())
                    debug("Received data from unauthorized address " + packet.getAddress() + ".");
            } catch (SocketTimeoutException sx) {
                if (chip.hasListeners()) debug("No data is available.");
            } catch (IOException ex) {
                Logger.getLogger(ipreceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (outputlen<2) return error("Expecting at least 2 outputs. 1 output clock pin and 1 or more data pins.");
        if (inputlen!=1) return error("Expecting 1 clock input.");
        if (args.length<2) return error("Expecting a port sign argument and at least one authorized incoming address.");

        try {
            int port = Integer.decode(args[0]);
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(SO_TIMEOUT);
                info("Listening on port " + socket.getLocalPort());
            } catch (BindException be) {
                return error("Port " + port + " is already used.");
            } catch (SocketException ex) {
                return error("Socket exception: " + ex);
            }
        } catch (NumberFormatException ne) {
            return error("Bad port number: " + args[1]);
        }

        // incoming addresses
        if (args.length==2 && args[1].equalsIgnoreCase("any"))
            authorizedAddresses = null;
        else {
            authorizedAddresses = new ArrayList<>();

            for (int i=1; i<args.length; i++) {
                try {
                    authorizedAddresses.add(InetAddress.getByName(args[i]));
                } catch (UnknownHostException ex) {
                    return error("Unknown host: " + args[i]);
                }
            }
        }

        buf = new byte[(int)Math.ceil((outputlen-1)/8d)];
        packet = new DatagramPacket(buf, buf.length);

        return this;

    }

    @Override
    public void shutdown() {
        if (socket!=null) socket.close();
    }
}
