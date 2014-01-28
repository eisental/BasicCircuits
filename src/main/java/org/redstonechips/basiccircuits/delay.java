
package org.redstonechips.basiccircuits;

import org.redstonechips.chip.Circuit;
import org.redstonechips.parsing.UnitParser;

/**
 *
 * @author Tal Eisenberg
 */
public class delay extends Circuit {
    private long interval;
    private long intervalInTicks;

    @Override
    public void input(final boolean state, final int inIdx) {
        if (intervalInTicks==0)
            write(state, inIdx);
        else rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, new Runnable() {
            @Override
            public void run() {
                write(state, inIdx);
            }
        }, intervalInTicks);
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen!=outputlen) return error("Expecting the same number of inputs and outputs.");
        if (inputlen==0) return error("Expecting at least one input and one output.");

        if (args.length==0) interval = 1000; // 1 sec default

        if (args.length>=1) {
            try {
                interval = Math.round(UnitParser.parse(args[0]));
            } catch (IllegalArgumentException e) {
                return error("Bad pulse duration argument: " + args[0]);
            }
        }

        intervalInTicks = Math.round(interval/50);

        return this;
    }
}
