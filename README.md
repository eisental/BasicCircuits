BasicCircuits
=============

The basic circuit library for [RedstoneChips](http://eisental.github.com/RedstoneChips).

__For much more information, visit the [circuitdocs](http://eisental.github.com/RedstoneChips/circuitdocs).__

Installation
-------------
* [Download](http://eisental.github.com/RedstoneChips) the latest RedstoneChips version bundle.
* Copy all jar file to the plugins folder of your craftbukkit installation.

Changelog
---------

#### BasicCircuits 0.97 (28th April, 2013)
- __pixel__: Added a maximum distance value preference to prevent lags and server crashes. The max can be changed using `/rcprefs pixel.maxDistance x` and defaults to 7.
- __sram__: Fixed a problem with anonymous memory.

#### BasicCircuits 0.96 (Dec 1st, 2012)
- New ramwatch chip for notifying memory data changes (@jpfx1342).
- dregister: Chip can now be backed by ram (@jpfx1342).
- repeater: Chip can now repeat sets of bits (@jpfx1342).
- sram-fix: now properly releases memory objects when destroyed (@jpfx1342).
- sram-fix: dataChanged() called readMemory() even when chip is disabled.
- display-fix: ram-backed display now stops updating after destruction (@jpfx1342).

#### BasicCircuits 0.95 (Apr 8th, 2012)
- Updated for RC0.95
- __display__: Rewrote the chip and added support for directly displaying ram memory contents.
- __burst__: Added pulse rate. Now can pulse at slower rates. It takes an additional sign argument in the same format as the frequency used for the clock chip.
- __sram__: outputs are immediately updated if another sram on the same memory writes on the same address.
- __clock__: Clock stops when chip is disabled.

#### BasicCircuits 0.94 (Jan 9th, 2012)
- Updated for RC0.94.
- __comparator__: New option for adding a clock input pin. 
- __shiftregister__: Added right shift. 
- __terminal__: Added wireless. 
- __display__: supports 1 line screens.
- __display__: The chip will work even when not all screen wool blocks are placed. 
- __display__: fixed screen not being cleared on activation.
- __display__: shows debug message also when setting a pixel to the same color. 
- __print__: when clock input is on any data change will cause signs to update.
- __print__: Removed spaces between words in binary mode.
- Doesn't print "Created new sram folder" message on every server startup. 
- Fixed sram /rctype dump crashing the server (http://github.com/eisental/BasicCircuits/issues/9).

#### BasicCircuits 0.93 (19/12/11)
- Updated to work with RC0.93.
- __display__: size argument is now optional. 
- __synth__: Added wireless support.
- __print__: Added wireless support.
- __print__: Removed scheduling of sign update. Seems to work. If your server freezes let me know…
- __print__: Fixed a bug that caused print to malfunction after a chunk reload.
- __sram__: Some bug fixes and added zero padding to `/rctype dump` address numbers.
- __sram__: Moved sram data files into their own folder. Should automatically move data files to the right place.
- __receiver__: Fixed channel length calculation (it was off by 1 before). 

#### BasicCircuits 0.92 (4/12/11)
- Updated to work with RedstoneChips 0.92.
- Fixed an NPE bug in wireless chips.
- __print__: Fixed some bugs. 
- __pulse__: A pulse with multiple outputs can be triggered by 1 input. The chip will wait until a pulse is over before moving to the next output in this mode.
- __display__: Creates a wool display with individually addressable pixels.
- __sram__: Changed file format slightly. Old format should still work.
- __sram__: 2 sram chips with the same id can share data in memory and on file.
- __sram__: should support any address size or word lengths. Previously it was limited to 32-bit integers.
- __sram__: memory ids must start with a letter and may contain letters digits or underscores (_).
- __sram__: Using `/rctype dump` on the chip sign will print its memory contents.
- __transmitter__: transmits on every input change when it's clock input is on.
- __transmitter__: new select mode for dynamically changing the broadcast start bit.
- __bust__: new chip that makes it possible to send any number of pulses per clock tick (based on pulsar by @needspeed10 ). 
- __clock__: doesn't clear it's outputs on circuit shutdown to prevent LWC from printing an error.
- counter, dregister, flipflop, pisoregister, shiftregister, sram, srnor resets its outputs on init.
- __ipreceiver__: Added option to use 'any' as a sign argument to receive data from any address.
- __transmitter__: fixed a small print bug.
- __nand__: fixed the chip.
- __clock__: Can transmit on a wireless channel by specifying one as a last argument, using the format: #<channel name>[:<start bit>].

#### BasicCircuits 0.9 (23/04/11)
- Updated to work with RedstoneChips 0.9.
- New bintobcd circuit for converting binary to bcd for numbers of any size.
- Small change in receiver and transmitter activation message.
- __counter__: Changed debug messages and possibly fixed the way it updates its outputs.
- __clock__: Rewrote the circuit without using threads. Added latency compensation to prevent overall timing drift and tick rounding errors. It's now possible to use 1 input to control any number of clock outputs. Pulse width of 1 also works, and like 0 pulse width, result in lower cpu use.
- __pixel__: It's now possible to change the wool painting distance of the pixel circuit using sign arguments. To set, add an argument anywhere with d{<distance>} or dist{<distance>}. The distance is the number of steps away from its interface blocks that the circuit will reach when painting wool blocks.
- __sram__: Added the option to use ascii strings in /rctype by typing /rctype ascii <text> and use ascii characters instead of numbers as word values. Added binary input to /rctype by using bXXXXX as a data value. For ex: /rctype 0:b0101. New readonly mode by adding readonly as 2nd sign argument. When used the chip doesn't have a read/write input control pin and data input pins. /rctype can still be used to edit memory contents.
- __terminal__: When an eot argument is added as a 2nd argument in ascii mode it will send an EOT ascii character (0x03 or ^C) after all characters were sent out. Added an optional EOT (end of transmission) output pin to the terminal circuit in ascii mode that is triggered after the last character is sent out. 
- __print__: The circuit will ignore ascii control characters (0-0x1F). The circuit's signs are now part of the circuit's structure. Breaking them will deactivate the circuit.
- __comparator__: Added debug messages.

bug fixes:

- Fixed bug when trying to use a the print circuit with no signs attached. print will not activate unless it has at least 1 sign to print on. The signs must be actually attached to the interface block now. Post signs can be used when placing them on top of the interface block.
- Fixed the zero wordlength crash bug in adder, multiplier and divider (thanks @Badzilla)
- sram will properly load its state after a server restart. An error is sent to the console when the circuit's memory file is not found.
- Fixed a bug where print circuit would print more than 15 characters on the 4th sign line.

#### BasicCircuits 0.88 (07/04/11)
- Updated to work with RC0.88.
- Decoder is less strict now, and can be built with less than the amount of outputs required for the number of inputs.
- Divider now checks for division by zero and the modulus wordlength bug is fixed.
- receiver and transmitter report their channel and start-end bits on activation.
- sram reports its memory size and file name on activation and will save it`s memory to file every time the plugin saves its circuits file.

#### BasicCircuits 0.87 (30/03/11)
- New subtract mode added to the adder circuit. 
- Updated receiver, pixel and transmitter to work with the new channel system.
- New sram memory circuit stores memory data in separate text files.
- New dregister circuit.
- New round mode and modulus mode added to divider.
- segdriver can have a blanking pin by adding 'blankPin' as a sign argument.
- shiftregister can have a 3rd reset pin to clear its outputs.
- Pixel will not color its interface blocks when they're made of wool.
- Fixed bug where pixel will keep receiving data wirelessly after it was destroyed.
- synth chips can have more than 5 data pins when using indexed mode finally making infinite tunes possible
- Fixed constant bug and typo in multiplier circuit.
- terminal will properly save its state when its shutdown.

#### BasicCircuits 0.84 (10/03/11)
- Updated to work with RedstoneChips 0.84.

#### BasicCircuits 0.83 (07/03/11)
- New repeater circuit.
- New inverted gates: nor, nand and xnor.
- New segdriver circuits for running a 7-segment display digit.
- pixel will now only color wool blocks that are attached or indirectly attached (through other wool blocks) to its interface blocks.
- router doesn't require a clock input when only 1 data pin is used. 
- router now accepts the 'all' keyword for routing an input to all of the chips outputs, e.g. `0:all` will route the 1st data input to each output.
- Fixed flipflop, pisoregister, shiftregister and srnor to properly restore their state after a server restart.
- Fixed counter init bug, direction is now set according to the direction pin if one exists.
- Updated to work with RedstoneChips 0.83.

#### BasicCircuits 0.82 (28/02/11)
- counter can now have a 3rd input pin for switching count direction.
- terminal circuit can have an optional clear input pin for setting all its inputs to 0.
- Updated to work with latest craftbukkit and RC0.82.
- Fixed bug in clock's pulse width error message.

#### BasicCircuits 0.8 (14/02/11)
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

