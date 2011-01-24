BasicCircuits 0.6
==================

The basic circuit package for [RedstoneChips](http://github.com/eisental/RedstoneChips). 
Includes many different kinds of chips that can handle binary numbers of any length (although sometimes limited to 32bit integers).

### Check the [wiki](https://github.com/eisental/BasicCircuits/wiki) for a full circuit list and more info on building and activating circuits.

__If you like this, please__

[![Donate](/eisental/RedstoneChips/raw/master/images/btn_donate_LG.gif")](http://sites.google.com/site/eisental/home/donate)

Installation
-------------
   * Delete any old versions of BasicCircuits and RedstoneChips
   * install [RedstoneChips](http://github.com/eisental/RedstoneChips) 0.6.
   * Download [jar file](/eisental/BasicCircuits/BasicCircuits-0.6.jar).
   * copy the jar file to your craftbukkit plugins folder.

Commands
---------
* __/redchips-channels__ Lists currently used broadcast channels.

Changelog
---------
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

