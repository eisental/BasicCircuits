BasicCircuits
=============

The basic circuit package for [RedstoneChips](http://eisental.github.com/RedstoneChips).

__For much more information, visit the [circuitdocs](http://eisental.github.com/RedstoneChips/circuitdocs).__

Installation
-------------
* If you're updating from a previous version, delete any previously installed RedstoneChips and BasicCircuits jar files and rename your <craftbukkit>/plugins/RedstoneChips-XX folder to /RedstoneChips (or delete it to remove previous settings).
* Download the [RedsoneChips-0.8](https://github.com/downloads/eisental/RedstoneChips/RedstoneChips-0.8.jar) jar file.
* Download the [BasicCircuits-0.8](https://github.com/downloads/eisental/BasicCircuits/BasicCircuits-0.8.jar) jar file.
* Copy the downloaded jar files into the plugins folder of your craftbukkit installation, keeping their original filenames.



Changelog
---------
#### BasicCircuit 0.8 (14/02/11)
- New delay circuit - delay any number of input signal for a fixed time duration.
- When a receiver has more than 1 output, it's 1st output now becomes an output clock pin, pulsing shortly everytime after new data is received.
- Renamed decadecounter to ringcounter. Apologies to anybody who is using it.
- adder, multiplier and divider now require a wordlength sign arg to define the number of bits each input set has. They can all have any number of outputs and a warning is sent if not enough outputs are used. The constant argument is the 2nd argument now.
- All circuits can gracefully reinitialize after a server restart without changing their state or losing information.
- pixel can receive input changes wirelessly and can be built without any inputs.
- receiver and transmitter work with the new TransmittingCircuit and ReceivingCircuit interfaces and can communicate with other implementing circuits such as pixel.
- print sign updates should work 90% of the time. print also has a new display mode for scrolling text and supports a clear pin when using add or scroll. Pointing at the prints activation sign and using /rc-type will now set the output signs' text accordingly. It will also save it's text buffer and restore it on server restart.
- synth circuit accepts flat notes, using a b sign: c2 eb2 g2 for ex. is the same as c2 d#2 g2.
- pulse and clock will display an error message when an invalid pulse duration argument is used.
- Moved /redchips-channels and /rc-type commands to RedstoneCihps
- Transmitters/receivers list is now handled by RedstoneCihps


#### BasicCircuits 0.77 (7/02/11)
- new comparator circuit for comparing binary numbers.
- iptransmitter and ipreceiver are enabled again.
- new iptransmitter.ports circuit preference key for setting the port range iptransmitter is allowed to use.
- ipreceiver now uses a clock input for receiving new data and has a clock output pin to which a pulse is sent
once new data is received.
- clock circuit now works with the new bukkit scheduler. Timing is now much much better (hooray!) and the clock will not crash the server.
- clock circuits with 0 pulse width should now perform much better.
- pulse circuit now works with the bukkit scheduler as well. 

#### BasicCircuits 0.76 (and 0.75) (4/02/11)
- Support the new library loading mechanism in RedstoneChips 0.76.
- Updated pixel, print and synth to work with the interface block changes. print output signs can be attached to any side of the interface block and multiple signs per interface block are supported. pixel's center is now the inerface block itself, wool can be added anywhere around it. synth noteblocks can be attached to any face of the interface block, including multiple noteblocks per interface block.
- synth circuits will now PLAY the note when the clock pin is triggered instead of just changing the noteblock's pitch. No extra triggering is needed for the noteblock. 
- Fixed bug in counter when using it without arguments.

#### BasicCircuits 0.74 (31/01/11)
* Added pulse argument for positive, negative and double edge-triggering.
* Circuit classes are now disabled when the plugin is disabled.
* Updated to work with the new bukkit command api.
* Router now uses a clock input. If two or more inputs are routed to the same output they're now ORed together.
* New circuit: decadecounter. Commissioned by I D
* Pulse circuit with a 0 pulse length will not create a thread and is now very safe to use.

#### BasicCircuits 0.73 (29/01/11)
* New terminal and router circuits.

#### BasicCircuits 0.72 (28/01/11)
* New SR NOR latch.

#### 0.71 (28/01/11)
* Fixed bug in counter circuit. It will now work properly without any sign arguments.
* New flipflop reset mode. It's possible to activate a flipflop with one extra reset input pin (input 0). When the reset pin
  is triggered all flipflops in the chip reset to off state.

#### 0.7 (27/01/11)
* iptransmitter and ipreceiver are disabled for the time being.
* New synth circuit for controlling noteblocks.
* Support for 1-bit pixel circuits.
* New counter sign arguments, min, max and direction.
* Added debug messages to counter.
* Changes to pixel, synth, and print to support multiple interface blocks.
* encoder now only requires that the number of inputs be less than or equal to the maximum number that can be represented by its outputs.
* clock is now limited to a minimum interval of 200ms.
* fixed some bugs and added debug messages to pisoregister and receiver.

#### 0.6 (24/01/11)
* new [iptransmitter](/eisental/BasicCircuits/wiki/Iptransmitter) and [ipreceiver](/eisental/BasicCircuits/wiki/Ipreceiver) circuits for your inter-planetary communication needs.
* new [pulse](/eisental/BasicCircuits/wiki/Pulse) circuit and a [not](/eisental/BasicCircuits/wiki/Not) gate circuit.
* [clock](/eisental/BasicCircuits/wiki/Clock) circuit now supports variable pulse widths.
* added send input pin to [transmitter](/eisental/BasicCircuits/wiki/Transmitter) circuit (thanks RustyDagger).
* fixed a bug in [shiftregister](/eisental/BasicCircuits/wiki/Shiftregister).
* new command /redchips-channels lists currently used broadcast channels.

#### 0.4 (22/01/11)
* NEW [pixel](/eisental/BasicCircuits/wiki/Pixel) circuit using colored wool as display pixels.
* print must have at least 2 inputs now.
* counter must have at least 1 input now.
* clock circuit is much more stable.


#### 0.2 (20/01/11)
* fixed a bug in decoder circuit.
* removed unnecessary log messages.

