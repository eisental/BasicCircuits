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
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class ipreceiver extends Circuit {

    DatagramSocket socket;
    ReceiverThread thread = new ReceiverThread();
    List<InetAddress> authorizedAddresses;
    byte[] buf;
    boolean closeConnection = false;
    DatagramPacket packet;
    private final static int SO_TIMEOUT = 20;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (high) {
            try {
                socket.receive(packet);
                if (authorizedAddresses.contains(packet.getAddress())) {
                    byte[] bytes = packet.getData();
                    BitSet7 bits = BitSet7.valueOf(bytes);
                    sendBitSet(bits);
                    if (hasDebuggers()) debug("Received " + Circuit.bitSetToBinaryString(bits, 0, outputs.length) + " from " + packet.getAddress() + ":" + packet.getPort());
                } else if (hasDebuggers())
                    debug("Received data from unauthorized address " + packet.getAddress() + ".");
            } catch (SocketTimeoutException sx) {
                if (hasDebuggers()) debug("Nothing to receive.");
            } catch (IOException ex) {
                Logger.getLogger(ipreceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (outputs.length==0) {
            error(player, "Expecting at least 1 output.");
            return false;
        } if (inputs.length!=1) {
            error(player, "Expecting 1 clock input.");
            return false;
        }

        if (args.length<2) {
            error(player, "Expecting a port sign argument and at least one authorized incoming address.");
            return false;
        }

        try {
            int port = Integer.decode(args[0]);
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(SO_TIMEOUT);
                info(player, "Listening on port " + socket.getLocalPort());
            } catch (BindException be) {
                error(player, "Port " + port + " is already used.");
                return false;
            } catch (SocketException ex) {
                error(player, "Socket exception: " + ex);
            }
        } catch (NumberFormatException ne) {
            error(player, "Bad port number: " + args[1]);
            return false;
        }

        // incoming addresses
        authorizedAddresses = new ArrayList<InetAddress>();
        
        for (int i=1; i<args.length; i++) {
            try {
                authorizedAddresses.add(InetAddress.getByName(args[i]));
            } catch (UnknownHostException ex) {
                error(player, "Unknown host: " + args[i]);
                return false;
            }
        }

        buf = new byte[(int)Math.ceil(outputs.length/8)];
        packet = new DatagramPacket(buf, buf.length);

        return true;

    }

    private class ReceiverThread extends Thread {

        @Override
        public void run() {
            while (true) {
                byte[] buf = new byte[(int)Math.ceil(inputs.length/8)];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    if (authorizedAddresses.contains(packet.getAddress())) {
                        byte[] bytes = packet.getData();
                        BitSet7 bits = BitSet7.valueOf(bytes);
                        sendBitSet(bits);
                        if (hasDebuggers()) debug("Received " + Circuit.bitSetToBinaryString(bits, 0, bits.length()) + " from " + packet.getAddress() + ":" + packet.getPort());
                    } 
                } catch (IOException ex) {
                    Logger.getLogger(ipreceiver.class.getName()).log(Level.SEVERE, null, ex);
                }

                synchronized(this) {
                    if (closeConnection) {
                        socket.close();
                        if (hasDebuggers()) debug("Closing socket connection on port " + socket.getLocalPort());
                        closeConnection = false;
                        return;
                    }
                }
            }
        }
    }

}
