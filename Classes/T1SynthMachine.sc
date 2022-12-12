/*

         88
  ,d     88
  88     88
MM88MMM  88  ,adPPYba,   ,adPPYba,
  88     88  I8[    ""  a8"     "8a
  88     88   `"Y8ba,   8b       d8
  88,    88  aa    ]8I  "8a,   ,a8"
  "Y888  88  `"YbbdP"'   `"YbbdP"'
         88

Copyright Torso Electronics 2022
All rights reserved.

*-0-*-0-*-0-*-0-*-0-*-0-*-0-*-0-*-0-*

T1SynthMachine.sc

*/

/*
*
* This class can connect to a T1 hardware device and map different synths to different channels
* TODO:
* - Choose between mono and poly synth
*
*/

T1SynthMachine{

    // An array containing a synth def name per midi channel. The index is the midi channel, the item is the synthdef name as a symbol
    var <synthDefAssignments;
    var <synths;
    var numChannels = 16;
    var notesPerChannel = 128;

    var <t1device;

    *start{
        ^super.new.init()
    }

    init{
        "%: Starting T1 device".format(this.class.name).postln;
        t1device = T1Device.start(debugMode: false);

        // Voices are handled as Synths. One per note in each channel
        synths = numChannels.collect{
            notesPerChannel.collect{
                nil
            }
        };

        // Set default synth as default synth for all channels
        synthDefAssignments = \default!numChannels;
        synthDefAssignments.do{|synthDefName, index|
            this.setSynthDefForChannel(synthDefName, index)
        };

        // Set note on functionality
        t1device.setNoteOnFunc({|val, num, chan|
            var ampScale = 0.5;
            var synthdefForThisChannel = synthDefAssignments[chan];

            // Set up arguments for the synth
            var args = [
                \freq, num.midicps,
                \gate, 1,
                \amp, val / 128.0 * ampScale
            ];

            // Spawn synth
            synths[chan][num] = Synth(
                synthdefForThisChannel,
                args
            );

        });

        // Set note off functionality
        t1device.setNoteOffFunc({|val, num, chan|
            // FIXME: This is an arbitrary release time
            synths[chan][num].release();
        });

    }

    setSynthDefForChannel{|synthDefName, midiChannel|
        synthDefAssignments[midiChannel] = synthDefName;
    }
}
