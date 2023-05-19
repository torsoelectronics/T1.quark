(
// Electro drum kit by snappizz
// https://sccode.org/1-523
SynthDef(\kick, {
    |out = 0, pan = 0, amp = 0.3|
    var body, bodyFreq, bodyAmp;
    var pop, popFreq, popAmp;
    var click, clickAmp;
    var snd;

    // body starts midrange, quickly drops down to low freqs, and trails off
    bodyFreq = EnvGen.ar(Env([261, 120, 51], [0.035, 0.08], curve: \exp));
    bodyAmp = EnvGen.ar(Env.linen(0.005, 0.1, 0.3), doneAction: 2);
    body = SinOsc.ar(bodyFreq) * bodyAmp;
    // pop sweeps over the midrange
    popFreq = XLine.kr(750, 261, 0.02);
    popAmp = EnvGen.ar(Env.linen(0.001, 0.02, 0.001)) * 0.15;
    pop = SinOsc.ar(popFreq) * popAmp;
    // click is spectrally rich, covering the high-freq range
    // you can use Formant, FM, noise, whatever
    clickAmp = EnvGen.ar(Env.perc(0.001, 0.01)) * 0.15;
    click = LPF.ar(Formant.ar(910, 4760, 2110), 3140) * clickAmp;

    snd = body + pop + click;
    snd = snd.tanh;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

SynthDef(\snare, {
    |out = 0, pan = 0, amp = 0.3|
    var pop, popAmp, popFreq;
    var noise, noiseAmp;
    var snd;

    // pop makes a click coming from very high frequencies
    // slowing down a little and stopping in mid-to-low
    popFreq = EnvGen.ar(Env([3261, 410, 160], [0.005, 0.01], curve: \exp));
    popAmp = EnvGen.ar(Env.perc(0.001, 0.11)) * 0.7;
    pop = SinOsc.ar(popFreq) * popAmp;
    // bandpass-filtered white noise
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.15), doneAction: 2);
    noise = BPF.ar(WhiteNoise.ar, 810, 1.6) * noiseAmp;

    snd = (pop + noise) * 1.3;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

SynthDef(\hihat, {
    |out = 0, pan = 0, amp = 0.3|
    var click, clickAmp;
    var noise, noiseAmp;
    var snd;

    // noise -> resonance -> expodec envelope
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.3, curve: -8), doneAction: 2);
    noise = Mix(BPF.ar(ClipNoise.ar, [4010, 4151], [0.15, 0.56], [1.0, 0.6])) * 0.7 * noiseAmp;

    snd = noise;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

// adapted from a post by Neil Cosgrove (other three are original)
SynthDef(\clap, {
    |out = 0, amp = 0.5, pan = 0, dur = 1|
    var env1, env2, snd, noise1, noise2;

    // noise 1 - 4 short repeats
    env1 = EnvGen.ar(
        Env.new(
            [0, 1, 0, 0.9, 0, 0.7, 0, 0.5, 0],
            [0.001, 0.009, 0, 0.008, 0, 0.01, 0, 0.03],
            [0, -3, 0, -3, 0, -3, 0, -4]
        )
    );

    noise1 = WhiteNoise.ar(env1);
    noise1 = HPF.ar(noise1, 600);
    noise1 = LPF.ar(noise1, XLine.kr(7200, 4000, 0.03));
    noise1 = BPF.ar(noise1, 1620, 3);

    // noise 2 - 1 longer single
    env2 = EnvGen.ar(Env.new([0, 1, 0], [0.02, 0.18], [0, -4]), doneAction:2);

    noise2 = WhiteNoise.ar(env2);
    noise2 = HPF.ar(noise2, 1000);
    noise2 = LPF.ar(noise2, 7600);
    noise2 = BPF.ar(noise2, 1230, 0.7, 0.7);

    snd = noise1 + noise2;
    snd = snd * 2;
    snd = snd.softclip;

    Out.ar(out, Pan2.ar(snd,pan,amp));
}).add;

// rhodes by snappizz
SynthDef(\rhodesey, {
	//FM Rhodes Synthethizer
    |
    // standard meanings
    out = 0, freq = 440, gate = 1, pan = 0, amp = 0.1,
    // all of these range from 0 to 1
    vel = 0.8, modIndex = 0.2, mix = 0.2, lfoSpeed = 0.4, lfoDepth = 0.1
    |
    var env1, env2, env3, env4;
    var osc1, osc2, osc3, osc4, snd;

    lfoSpeed = lfoSpeed * 12;

    freq = freq * 2;

    env1 = EnvGen.ar(Env.adsr(0.001, 1.25, 0.0, 0.04, curve: \lin));
    env2 = EnvGen.ar(Env.adsr(0.001, 1.00, 0.0, 0.04, curve: \lin));
    env3 = EnvGen.ar(Env.adsr(0.001, 1.50, 0.0, 0.04, curve: \lin));
    env4 = EnvGen.ar(Env.adsr(0.001, 1.50, 0.0, 0.04, curve: \lin));

    osc4 = SinOsc.ar(freq * 0.5) * 2pi * 2 * 0.535887 * modIndex * env4 * vel;
    osc3 = SinOsc.ar(freq, osc4) * env3 * vel;
    osc2 = SinOsc.ar(freq * 15) * 2pi * 0.108819 * env2 * vel;
    osc1 = SinOsc.ar(freq, osc2) * env1 * vel;
    snd = Mix((osc3 * (1 - mix)) + (osc1 * mix));
    snd = snd * (SinOsc.ar(lfoSpeed) * lfoDepth + 1);

    // using the doneAction: 2 on the other envs can create clicks (bc of the linear curve maybe?)
    snd = snd * EnvGen.ar(Env.asr(0, 1, 0.1), gate, doneAction: 2);
    snd = Pan2.ar(snd, pan, amp * 0.5);

    Out.ar(out, snd);
//By Nathan Ho aka Snappizz
//http://sccode.org/1-522
}).add;

SynthDef(\organ_tonewheel1, {
	//additive tonewheel organ with more CPU usage

	arg freq = 440, amp = 0.7, atk = 0.001, sus = 0.2, rel = 0.01, pan = 0,
	bass = 1, quint = 1, fundamental = 1, oct = 1, nazard = 1, blockFlute = 1, tierce = 1, larigot = 1, sifflute = 1, //organ voices (drawbars) amplitudes
    vrate = 3, vdepth = 0.008, vdelay = 0.1, vonset = 0, vrateVariation = 0.1, vdepthVariation = 0.1; //vibrato arguments
	var sig, env, vibrato;

	vibrato = Vibrato.kr(DC.kr(freq), DC.kr(vrate), DC.kr(vdepth), DC.kr(vdelay), DC.kr(vonset), DC.kr(vrateVariation), DC.kr(vdepthVariation));
	env = EnvGen.ar(Env.linen(atk, sus, rel), doneAction: Done.freeSelf);
	sig = DynKlang.ar(`[[1/12,  1/7, 1, 12, 19, 24, 28, 31, 36].midiratio, ([DC.ar(bass) , DC.ar(quint), DC.ar(fundamental), DC.ar(oct), DC.ar(nazard), DC.ar(blockFlute), DC.ar(tierce), DC.ar(larigot), DC.ar(sifflute)].normalizeSum), nil], vibrato);

	sig = sig * env;
	Out.ar(0, Pan2.ar(sig, pan, amp));
//By Zé Craum
}).add;

// Simple bass by Mads
SynthDef(\sineperc, {| freq = 100, out = 0, dur = 0.1, amp = 0.1, pan = 0, gate = 1, atk = 0.01, rel = 0.1, curve = -4 |
    var env = EnvGen.kr(Env.perc(atk, rel, curve: curve), gate, doneAction: 2);
    var snd = SinOsc.ar(freq, 0, amp * env);
    Out.ar(out, Pan2.ar(snd, pan));
}).add;

// Based on https://sccode.org/1-5gi
SynthDef(\dex11algo8,// all out
    {
        |
        freq=150,
        op1att=0.001,op1dec=5.250,op1amt=1,op1tune=0,
        op2att=0.001,op2dec=1.350,op2amt=1,op2tune=0,
        op3att=0.01,op3dec=1.150,op3amt=1,op3tune=0,
        op4att=0.001,op4dec=1.250,op4amt=0.5,op4tune=0,feedbackamt=1.95, amp=0.25|
        var env1,env2,env3,env4,op1,op2,op3,op4,pan=(0.0),signal;

        env4=EnvGen.ar(Env([0,1,0],[op4att,op4dec],[0,-5]),doneAction:0);
        op4=SinOscFB.ar(freq+op4tune,env4*feedbackamt);
        op4=op4*env4;
        op4=op4*op4amt;
        op4;
        env3=EnvGen.ar(Env([0,1,0],[op3att,op3dec],[0,-5]),doneAction:0);
        op3=SinOsc.ar(freq+op3tune);
        op3=op3*env3;
        op3=op3*op3amt;
        env2=EnvGen.ar(Env([0,1,0],[op2att,op2dec],[0,-5]),doneAction:0);
        op2=SinOsc.ar(freq+op2tune);
        op2=op2*env2;
        op2=op2*op2amt;
        env1=EnvGen.ar(Env([0,1,0],[op1att,op1dec],[0,-5]),doneAction:2);
        op1=SinOsc.ar(freq+op1tune);
        op1=op1*env1;
        op1=op1*op1amt;
        signal=(op1+op2+op3+op4)/4;
        signal=Pan2.ar(signal,pos:pan);
        Out.ar(0,signal * amp * 0.5);
    }
).add;

SynthDef(\glockenspiel, {
	|freq = 440, t60=6, pitchy=1, amp=0.25, gate=1, pan = 0|
	var sig, exciter;
	exciter = WhiteNoise.ar() * EnvGen.ar(Env.perc(0.001, 0.05), gate) * 0.25;
	sig = DynKlank.ar(
		`[
			[1, 2, 2.803, 3.871, 5.074, 7.81, 10.948, 14.421],   // freqs
			[1, 0.044, 0.891, 0.0891, 0.794, 0.1, 0.281, 0.079], // amplitudes
			[1, 0.205, 1, 0.196, 0.339, 0.047, 0.058, 0.047]*t60     // ring times
		],
		exciter,
		freqscale: freq);
	DetectSilence.ar(sig, 0.001, 0.5, doneAction:2);
	Out.ar(0, Pan2.ar(sig, pan, amp) * 0.125);
//This is the same used for glockenspiel, xylophone, marimba and regular bell, what changes is only the ring time t60
//By nicolaariutti and edited by Zé Craum
//	http://sccode.org/1-5ay#c835
}).add;

SynthDef(\dex11algo5,//op2-->op1   op4-->op3

	{
		|
		freq=260,
		op1att=0.001,op1dec=5.250,op1amt=1,op1tune=0,
		op2att=0.001,op2dec=1.250,op2amt=1,op2tune=0,
		op3att=0.001,op3dec=1.250,op3amt=1,op3tune=0,
		op4att=0.001,op4dec=1.250,op4amt=1,op4tune=0,feedbackamt=0, amp=0.125|
		var env1,env2,env3,env4,op1,op2,op3,op4,pan=(0.0),signal;

		env4=EnvGen.ar(Env([0,1,0],[op4att,op4dec],[0,-5]),doneAction:0);
		op4=SinOscFB.ar(freq+op4tune,env4*feedbackamt);
		op4=op4*env4;
		op4=op4*op4amt;
		op4;
		env3=EnvGen.ar(Env([0,1,0],[op3att,op3dec],[0,-5]),doneAction:0);
		op3=SinOsc.ar(freq+op3tune,op4);
		op3=op3*env3;
		op3=op3*op3amt;
		env2=EnvGen.ar(Env([0,1,0],[op2att,op2dec],[0,-5]),doneAction:0);
		op2=SinOsc.ar(freq+op2tune);
		op2=op2*env2;
		op2=op2*op2amt;
		env1=EnvGen.ar(Env([0,1,0],[op1att,op1dec],[0,-5]),doneAction:2);
		op1=SinOsc.ar(freq+op1tune,op2);
		op1=op1*env1;
		op1=op1*op1amt;
		signal=(op1+op3)/2;
		signal=Pan2.ar(signal,pos:pan);
		Out.ar(0,signal * amp * 0.5);
}).add;

SynthDef(\openhat, {
    |out = 0, pan = 0, amp = 0.3|
    var click, clickAmp;
    var noise, noiseAmp;
    var snd;

    // noise -> resonance -> expodec envelope
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.85, curve: -8), doneAction: 2);
    noise = Mix(BPF.ar(ClipNoise.ar, [4010, 4151], [0.15, 0.56], [1.0, 0.6])) * 0.7 * noiseAmp;

    snd = noise;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

SynthDef(\closedhat, {
    |out = 0, pan = 0, amp = 0.3|
    var click, clickAmp;
    var noise, noiseAmp;
    var snd;

    // noise -> resonance -> expodec envelope
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.1, curve: -8), doneAction: 2);
    noise = Mix(BPF.ar(ClipNoise.ar, [4010, 4151], [0.15, 0.56], [1.0, 0.6])) * 0.7 * noiseAmp;

    snd = noise;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

// adapted from a post by Neil Cosgrove (other three are original)
SynthDef(\clap2, {
    |out = 0, amp = 0.5, pan = 0, dur = 1|
    var env1, env2, snd, noise1, noise2;

    // noise 1 - 4 short repeats
    env1 = EnvGen.ar(
        Env.new(
            [0, 1, 0, 0.9, 0, 0.7, 0, 0.5, 0],
            [0.001, 0.009, 0, 0.008, 0, 0.01, 0, 0.03],
            [0, -3, 0, -3, 0, -3, 0, -4]
        )
    );

    noise1 = WhiteNoise.ar(env1);
    noise1 = HPF.ar(noise1, 600);
    noise1 = LPF.ar(noise1, XLine.kr(7200, 4000, 0.03));
    noise1 = BPF.ar(noise1, 1620, 3);

    // noise 2 - 1 longer single
    env2 = EnvGen.ar(Env.new([0, 1, 0], [0.02, 0.18], [0, -4]), doneAction:2);

    noise2 = WhiteNoise.ar(env2);
    noise2 = HPF.ar(noise2, 1000);
    noise2 = LPF.ar(noise2, 7600);
    noise2 = BPF.ar(noise2, 1230, 0.7, 0.7);

    snd = noise1 + noise2;
    snd = snd * 2;
    snd = snd.softclip;

    Out.ar(out, Pan2.ar(snd,pan,amp));
}).add;

SynthDef(\neurosnare, {
    |out = 0, pan = 0, amp = 0.7|
    var snd;
    // a percussive click to give it some attack
    snd = LPF.ar(HPF.ar(WhiteNoise.ar, 300), 8000) * Env.linen(0.001, 0.01, 0.001).ar;
    // sine sweep body. very important!
    snd = snd + (SinOsc.ar(Env([400, 196, 160], [0.04, 0.2], \exp).ar) * Env.perc(0.04, 0.2).ar * 6.dbamp).tanh;
    // sound of snare coils rattling
    snd = snd + (HPF.ar(BPeakEQ.ar(WhiteNoise.ar, 4000, 0.5, 3), 300) * Env.perc(0.05, 0.2).delay(0.01).ar(2) * -3.dbamp);
    // another sound sweep to improve the attack, optional
    snd = snd + (SinOsc.ar(XLine.kr(3000, 1500, 0.01)) * Env.perc(0.001, 0.02).ar);
    // distortion helps glue everything together and acts as a compressor
    snd = (snd * 1.4).tanh;
    Out.ar(out, Pan2.ar(snd, pan, amp * 0.4));
}).add;

SynthDef(\cowbell, {
    | out=0, amp=0.8, freq=540, pan=0.0, gate|
    var sig, env, hash;

    // hash = DC.ar(0);
    hash = BPF.ar(
        Hasher.ar(Sweep.ar(rate: TRand.kr(1.0, 10.0))),
        freq * 1.4,
        0.5,
        2
    ).tanh * Env.perc(0.001, 0.02).ar(gate: gate);
    sig = Pulse.ar( freq * [ 1, 1.5085 ], [ 0.565, 0.445 ], [ 0.4, 0.6 ] ).atan;
    env = EnvGen.ar(
        Env(
            [ 0, 0.05, 1, 0.1, 0 ], [ 0.003, 0.002, 0.05, 0.5 ], [2, -4, -4, -4]
        ),
        timeScale: [ 1.0, 1.5 ],
        doneAction: 2
    );
    sig = Mix( (hash + sig) * env );
    sig = BPF.ar( sig, freq * 2, 1.808 );

    Out.ar( out, Pan2.ar( sig, pan, amp ) );
}).add;

//******************
// FX SYNTHS
//******************
SynthDef(\distortion, {|out=0|
    var snd, env;
    var in = In.ar(out, 2);
    snd = in;

    snd = CrossoverDistortion.ar(in:snd, amp:\amp.kr(0.5, spec: [0.0, 1.0]), smooth:\smooth.kr(0.5, spec: [0.0, 1.0]));

    // Drywet
    snd = XFade2.ar(snd, in, \drywet.kr(0.5, spec: [-1.0, 1.0]));

    ReplaceOut.ar(out, snd);
}).add;

// JPVerb reverb
SynthDef(\jpverb, {
    arg out = 0, room = 0.5, damp = 0.5, mix = 0.5, amp = 0.5;
    var snd;
    var in = In.ar(out, 2);

    snd = JPverb.ar(in, room, damp, mix);

    // Drywet
    snd = XFade2.ar(in,snd, \drywet.kr(0.0, spec: [-1.0, 1.0]));

    snd = snd * amp;
    Out.ar(out, snd);
}).add;

)
