BasicCircuits
=============

The basic circuit package for [RedstoneChips](http://eisental.github.com/RedstoneChips).

__For much more information, visit the [circuitdocs](http://eisental.github.com/RedstoneChips/circuitdocs).__

Installation
-------------
* If you're updating from a previous version, delete any previously installed RedstoneChips and BasicCircuits jar files and rename your <craftbukkit>/plugins/RedstoneChips-XX folder to RedstoneChips-0.77 (or delete it to remove previous settings).
* Download the [RedsoneChips-0.77](https://github.com/downloads/eisental/RedstoneChips/RedstoneChips-0.77.jar) jar file.
* Download the [BasicCircuits-0.77](https://github.com/downloads/eisental/BasicCircuits/BasicCircuits-0.77.jar) jar file.
* Copy the downloaded jar files into the plugins folder of your craftbukkit installation, keeping their original filenames.



Changelog
---------

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

