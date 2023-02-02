// first open and eval global-function.sc

/**
Two parts to this file:

The first block is your global configuration (tempo, key signature, time signature).
It also generates the synthdefs by calling the global function synthGenFM.
You can update and re-evaluate any before or during playback to change the conf and synthdefs.

The second block is the player. It writes some melodies for a bass, lead, and harmony.
Then it reads the presets you made and starts to play it all together!

*/



(
// block one, Mini Meta Maker
var describeConf = (
	cpc: "Cycles Per Cell, describes how many cycles are in 1 measure of music",
	cps: "Cycles Per Second, describes tempo as (BPM/60)",
	root: "key signature as a value between 1 and 2",
	midikey: "chromatic index in 0-11 of the root"

);

var conf = (
	cpc: 16,
	cps: 2.1,
	root: 1.2,
	midikey: 1,
);

var presets = (
	bass: ~synthGenFM.value(conf, 2.pow(8), 2.pow(5), 2.pow(9), "bass"),
	lead: ~synthGenFM.value(conf, 2.pow(9), 2.pow(7), 2.pow(12), "lead"),
	harmony: ~synthGenFM.value(conf, 2.pow(9), 2.pow(7), 2.pow(12), "harmony"),
);


/*
If you moved the class version to your classpath, then you would do this instead:
var presets = (
	bass: SynthGen.fm(conf, 2.pow(8), 2.pow(5), 2.pow(9), "bass"),
	lead: SynthGen.fm(conf, 2.pow(9), 2.pow(7), 2.pow(12), "lead"),
	harmony: SynthGen.fm(conf, 2.pow(9), 2.pow(7), 2.pow(12), "harmony"),
);
*/


presets.keysValuesDo({|key, synthDef|
	["Adding synthdef", key, "to the server"].join(" ").postln;
	synthDef.add;
});

~conf = conf;
~presets = presets;

// THIS PART IS COOL
// You can re-evaluate this block during playback to change the synths live!

);



(
// block two, Mini Song Composer

// Uses a Western major scale to generate melodies, without attempting to sync bass and melody

var notes = [0, 2, 4, 5, 7, 9, 11];
var bassOffset = 36;
var leadOffset = 62;
var harmonyOffset= 74;

// Define the rhythm options
var durOptions = [1/2, 1, 3/2, 2, 3];

// Just pick some without a lot of thought
var bassRhythm = Array.fill(8, durOptions.choose );
var leadRhythm = (8 * 2).collect( { durOptions.choose } );

var bassMelody = bassRhythm.collect({|dur|	notes.choose });
var leadMelody = leadRhythm.collect({|dur|	notes.choose });

// Creates a harmony for the melody part that shares a rhythm with the melody

var harmMelody = leadMelody.collect({|chromatic|
	var index = notes.detectIndex({|x| x == chromatic});
	notes.wrapAt(index + 2);
});

// Convert to frequency values
// Also used in pfreq (previous frequency) for synth glide control
var bassFreqs = bassMelody.collect({|chromatic| (chromatic+bassOffset).midicps });
var leadFreqs = leadMelody.collect({|chromatic| (chromatic+leadOffset).midicps });
var harmFreqs = harmMelody.collect({|chromatic| (chromatic+harmonyOffset).midicps });

var dt = 1/32; // the smallest quantization for measuring time

var phraseParams = (
	cps: ~conf.at(\cps),
	cpc: ~conf.at(\cpc),
	root: ~conf.at(\root),
	t: Pfunc({~t})
);

var parts = (
	bass: [bassRhythm, bassFreqs, 0.3,  32],
	lead: [leadRhythm, leadFreqs, 0.2,  16],
	harmony: [leadRhythm, harmFreqs, 0.1, 6],
);

~t = 0;

TempoClock.tempo = ~conf.at(\cps);
Pbindef(\time_keeper,
	\dur, Pn(dt, inf),
	\update_t, Pfunc({ ~t = ~t + dt }),
	\amp, 0 // otherwise it is audible and we just want a clock
).play;

parts.keysValuesDo({|partName, d|
	var rhythm = d[0];
	var freqs = d[1];
	var amp = d[2];
	var phrase = d[3];


	Pbindef(partName,
		\instrument, ~presets.at(partName).name,
		\freq, Pseq(freqs, inf),
		\dur, Pseq(rhythm, inf),
		\amp, amp,
		\sus, Pfunc({ 0.5 + 0.5.rand }),
		\pfreq, Pseq(freqs.copy.addFirst(freqs.first).drop(-1), inf),
		\phrase, phrase,
		*phraseParams.asKeyValuePairs
	).play;
});

)
