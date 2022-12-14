# SuperCollider interface for the Torso Electronics T-1

[![https://torsoelectronics.com/](assets/t1.jpg)](https://torsoelectronics.com/t-1/)

This SuperCollider package adds easy-to-use interfaces for the [Torso Electronics T-1 Algorithmic Sequencer](https://torsoelectronics.com/t-1/).

Among other things, it makes it really easy to make custom interfaces for the T-1's many streams of MIDI. Or, if you just want to easily play a SynthDef on one of the T-1's midi channels, it's as easy as:

```supercollider
(
z = T1SynthMachine.start();
z.setSynthDefForChannel(synthDefName: \default, midiChannel: 0)
)
```

See the help files for more information.

## Installation

From within SuperCollider, run the following line of code to automatically install this package:

```supercollider 
Quarks.install("https://github.com/torsoelectronics/T1.quark")
```

And then recompile.
