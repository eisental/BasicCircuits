/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class ipreceiver extends Circuit {

    DatagramSocket socket;
    ReceiverThread thread = new ReceiverThread();
    boolean closeConnection = false;

    @Override
    public void inputChange(int inIdx, boolean newLevel) { }

    @Override
    protected boolean init(Player player, String[] args) {
        if (outputs.length==0) {
            error(player, "Expecting at least 1 output.");
            return false;
        }
        if (args.length!=1) {
            error(player, "Expecting a port sign argument.");
            return false;
        }

        try {
            int port = Integer.decode(args[0]);
            try {
                socket = new DatagramSocket(port);
                thread = new ReceiverThread();
                thread.start();
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

        return true;

    }

    @Override
    public void circuitDestroyed() {
        closeConnection = true;
    }

    private class ReceiverThread extends Thread {

        @Override
        public void run() {
            while (true) {
                byte[] buf = new byte[Math.max(1, inputs.length/8)];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    byte[] bytes = packet.getData();
                    BitSet7 bits = BitSet7.valueOf(bytes);
                    sendBitSet(bits);
                    if (hasDebuggers()) debug("Received " + Circuit.bitSetToBinaryString(bits, 0, bits.length()) + " from " + packet.getAddress() + ":" + packet.getPort());
                    System.out.println("received " + bits);
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
