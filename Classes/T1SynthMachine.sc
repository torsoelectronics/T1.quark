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
    var <synthDefArgs;
    var <synths;
    var numChannels = 16;
    var notesPerChannel = 128;

    var <t1device;
    var <>latency = 0.2;

    var <initialized = false;

    *start{|permanent=true|
        ^super.new.init(permanent)
    }

    init{|permanent|
        if(initialized.not, {
            "%: Starting T1 device".format(this.class.name).postln;
            t1device = T1Device.start(debugMode: false, permanent: permanent);

            // Voices are handled as Synths. One per note in each channel
            synths = numChannels.collect{
                notesPerChannel.collect{
                    nil
                }
            };

            // Set default synth as default synth for all channels
            synthDefArgs = Array.newClear(numChannels);
            synthDefAssignments = \default!numChannels;
            synthDefAssignments.do{|synthDefName, index|
                this.setSynthDefForChannel(synthDefName, index)
            };

            16.do{|trackNum|

                // Set note on functionality
                t1device.setNoteOnFunc(trackNum, {|val, num, chan|
                    var ampScale = 0.5;
                    var synthdefForThisChannel = synthDefAssignments[chan];
                    var argsForThisChannel = synthDefArgs[chan];

                    // Set up arguments for the synth
                    var args = [
                        \freq, num.midicps,
                        \gate, 1,
                        \amp, val / 128.0 * ampScale,
                        \pitch, num,
                        \velocity, val
                    ] ++ argsForThisChannel;

                    // Spawn synth
                    synths[chan][num] = Synth.basicNew(
                        synthdefForThisChannel,
                        Server.default
                    );

                    // Set arguments
                    Server.default.sendBundle(latency, synths[chan][num].newMsg(nil, args));

                });

                // Set note off functionality
                t1device.setNoteOffFunc(trackNum, {|val, num, chan|
                    // Check if synth has gate argument
                    var hasGate = SynthDescLib.global.synthDescs.at(synthDefAssignments[chan]).hasGate;

                    // Only release synth if it has gate argument
                    if (hasGate && synths[chan][num].notNil) {
                        // FIXME: This is an arbitrary release time
                        Server.default.sendBundle(latency, synths[chan][num].setMsg(\gate, 0));
                    }
                });
            };

            initialized = true;
        }, {
            "%: T1 device already intialized".format(this.class.name).postln;
        })
    }

    setSynthDefForChannel{|synthDefName, midiChannel, args|
        var hasGate = SynthDescLib.global.synthDescs.at(synthDefAssignments[midiChannel]).hasGate;

        if (hasGate) {
          synths[midiChannel].do {|synth|
            if (synth.notNil) {
              synth.release;
            }
          };
        };

        synthDefAssignments[midiChannel] = synthDefName;
        synthDefArgs[midiChannel] = args;
    }
}
