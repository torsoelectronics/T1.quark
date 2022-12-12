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

T1Device.sc

*/

/*
*
* This class is a convenient interface for connecting to a T1 device and registering responder functions to react to it's midi messages
*
* TODO:
* - CC: CC70 - CC83
* - Option to fix functions
*
*/

T1Device{

    // Dispatchers - these contain the MIDIFuncs responsible for receiving midi in SC
    var <noteOnDispatcher, <noteOffDispatcher, <stopDispatcher, <startDispatcher;

    // Used to hack MIDIIn to say whether our controller is connected or not
    var connectMethod = 'isT1Connected';

    *start{|debugMode=true|
        ^super.new.init(debugMode)
    }

    init{|useDefaultFunctions|
        this.connect();

        // Set default responders if necessary
        if(useDefaultFunctions, {
            "%: Using default responder functions".format(this.controllerName).postln;
            this.registerDefaultResponderFuncs()
        });
    }

    registerDefaultResponderFuncs{
        this.setNoteOnFunc(
            {|val, num, chan|
                "%: NoteOn message received: val %, num %, chan %".format(this.controllerName, val, num, chan).postln
            }
        );

        this.setNoteOffFunc(
            {|val, num, chan|
                "%: NoteOff message received: val %, num %, chan %".format(this.controllerName, val, num, chan).postln
            }
        );

        this.setStartFunc(
            {|val, num, chan|
                "%: Start message received: val %, num %, chan %".format(this.controllerName, val, num, chan).postln
            }
        );

        this.setStopFunc(
            {|val, num, chan|
                "%: Stop message received: val %, num %, chan %".format(this.controllerName, val, num, chan).postln
            }
        );
    }

    controllerName{
        ^"T-1"
    }

    // An overengineered way of connecting only this controller to SuperCollider. Much faster (at least on Linux) than connecting all.
    connect{
        // Connect midi controller
        if(MIDIClient.initialized.not, {
            "MIDIClient not initialized... initializing now".postln;
            MIDIClient.init;
        });

        // This ratsnest connects only this controller and not all, which is much faster than the latter.
        MIDIClient.sources.do{|src, srcNum|
            if(src.device == this.controllerName.asString, {
                if(try{MIDIIn.connectMethod}.isNil, {
                    var isSource = MIDIClient.sources.any({|e|
                        e.device==this.controllerName.asString
                    });

                    if(isSource, {
                        "Connecting %".format(this.controllerName).postln;

                        MIDIIn
                        .connect(srcNum, src)
                        .addUniqueMethod(connectMethod, {
                            true
                        });

                        "Connected %... ".format(this.controllerName).postln;
                    });
                }, {
                    "% is already connected... (device is busy)".format(
                        this.controllerName
                    ).warn
                });
            });
        };
    }

    setNoteOnFunc{|newFunc|
        if(noteOnDispatcher.isNil.not, {
            noteOnDispatcher.free();
        });

        noteOnDispatcher = MIDIFunc.noteOn(func:newFunc);
    }

    setNoteOffFunc{|newFunc|
        if(noteOffDispatcher.isNil.not, {
           noteOffDispatcher.free();
        });

        noteOffDispatcher = MIDIFunc.noteOff(func:newFunc);
    }

    setStartFunc{|newFunc|
        if(startDispatcher.isNil.not, {
            startDispatcher.free();
        });

        startDispatcher = MIDIFunc.start(func:newFunc);
    }

    setStopFunc{|newFunc|
        if(stopDispatcher.isNil.not, {
            stopDispatcher.free();
        });

        stopDispatcher = MIDIFunc.stop(func:newFunc);
    }
}
