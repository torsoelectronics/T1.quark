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

T1Track.sc

*/

// This class represents one track on the T-1, including it's note events and ccs
T1Track{
    const numCCSPerTrack = 14;
    classvar <knobNames;
    var <noteOnDispatcher, <noteOffDispatcher;
    var <channel;
    var <ccResponders;
    var <ccNums;

    var fixed;

    *new{|midiChannel, ccNumbers, debugMode=false, permanent=true|
        ^super.new.init(midiChannel, ccNumbers, debugMode, permanent)
    }

    init{|midiChannel, ccNumbers, debugMode, permanent|
        fixed = permanent;

        this.setMidiChannel(midiChannel);
        this.setCCNums(ccNumbers ? (70..83));

        knobNames = [
            \steps,
            \pulses,
            \cycles,
            \division,
            \velocity,
            \sustain,
            \pitch,
            \repeats,
            \time,
            \voicing,
            \range,
            \accent,
            \timing,
            \scale
        ];

        ccResponders = IdentityDictionary.new;
        knobNames.do{|knobName, index|
            ccResponders.put(knobName,
                (ccNum: ccNums[index], func: nil, responder: nil)
            )
        };

        // Set default responders if necessary
        if(debugMode, {
            "%: Using default responder functions".format("T-1").postln;
            this.registerDefaultResponderFuncs()
        });

    }

    // TODO: Update all responder functions with new channel
    setMidiChannel{|newChannel|
        channel = newChannel;
    }

    // TODO: Update cc responders
    setCCNums{|newCCNums|
        if(newCCNums.isKindOf(ArrayedCollection) && newCCNums.size == numCCSPerTrack, {
            ccNums = newCCNums;
        }, {
            "T-1: You have to set all ccs using an array of % integer values".format(numCCSPerTrack).error
        })
    }

    registerDefaultResponderFuncs{
        this.setNoteOnFunc(
            {|val, num, chan|
                "NoteOn message received: val %, num %, chan %".format(val, num, chan).postln
            }
        );

        this.setNoteOffFunc(
            {|val, num, chan|
                "NoteOff message received: val %, num %, chan %".format(val, num, chan).postln
            }
        );

        // Set CC responders
        knobNames.do{|knobName|
            this.setCCResponderFunc(knobName, {|val, num, chan|
                    "CC message received from knob %: val %, num %, chan %".format(knobName, val, num, chan).postln
                }
            )
        };

    }

    setCCResponderFunc{|knobName, newFunc|
        var knobNameExists = knobNames.indexOfEqual(knobName).notNil;

        if(knobNameExists, {
            var ccNum = ccResponders[knobName][\ccNum];
            ccResponders[knobName][\func] = newFunc;

            // Free old func if any
            if(ccResponders[knobName][\responder].isNil.not, {
                ccResponders[knobName][\responder].free();
            });

            // Register new func
            ccResponders[knobName][\responder] = MIDIFunc.cc(func:newFunc, ccNum:ccNum, chan:channel);

            // Persist between Cmd periods
            if(fixed, {
                ccResponders[knobName][\responder].permanent();
            })

        }, {
            "T-1: Knob name % does not exist".format(knobName).error
        })

    }

    setNoteOnFunc{|newFunc|
        // Free old if any
        if(noteOnDispatcher.isNil.not, {
            noteOnDispatcher.free();
        });

        // Register new func
        noteOnDispatcher = MIDIFunc.noteOn(func:newFunc, chan: channel);

        // Persist between Cmd periods
        if(fixed, {
            noteOnDispatcher.permanent();
        })

    }

    setNoteOffFunc{|newFunc|
        // Free old if any
        if(noteOffDispatcher.isNil.not, {
            noteOffDispatcher.free();
        });

        // Register new func
        noteOffDispatcher = MIDIFunc.noteOff(func:newFunc, chan: channel);

        // Persist between Cmd periods
        if(fixed, {
            noteOffDispatcher.permanent();
        })
    }
}
