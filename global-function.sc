/*
Here is the generator defined as a global variable whose value is a function.

When you want to update the generator, re-evaluate the code, then re-evaluate the places you call the function.

Start by evaluating the block below, then goto player.sc
*/

(

/*
Function Arguments:

conf:
Metadata about the song. Used to make the synth responsive to key, key signature, and tempo.

maxFreq:
Maximum note value this synth will play. Allows responsive design to buckets of low-mid-high

minHarmonic:
The lowest possible output frequency. Frequencies below are removed.

maxHarmonic:
The highest possible output frequency. Frequencies above are removed.

name:
Optional name for the SynthDef. If none is provided, a default name is generated.
This means it will replace the previous default synth.
*/

~synthGenFM = { arg conf, maxFreq, minHarmonic, maxHarmonic, name;
	var carrier = [SinOsc, LFSaw, LFPulse, LFTri].choose;

	var pan = (if (0.5.coin, -1, 1)) * 0.6.rand;
	var cps = conf.at(\cps), root = conf.at(\root), cpc = conf.at(\cpc);

	var modulators = [LFTri, LFSaw, SinOsc].scramble;

	var nCarriers = 3;
	var modsPerCarrier = 3;

	var highestOctave = [maxHarmonic, maxFreq].maxItem.log2;
	var maxOctave = 11; // General maximum value for frequencies to prevent aliasing

	var glide = [6,8,12,18,24, 48, 72, 96].choose.reciprocal;

	// When using more than 1 carrier, turn them down a decible per carrier so it doesn't get too loud
	var carScale = (1/nCarriers) * (1/3);

    // it helps to multiply the mod amount by a value between 10 and 1000.
	var modHelper = 10 + 10.exprand(1000);


	var numerator = if (highestOctave < 8, [2,3].choose,  [3,4,5].choose);
	var denominator = numerator - [1,2].choose;
	var modulationRatio = numerator / denominator;

	// Extra safetey check to prevent aliasing above the Nyquist frequency
	if (highestOctave > maxOctave, {
		// lower maxFreq and maxHarmonic to be under 22,000 Hz
		maxFreq = maxFreq * 2.pow(maxOctave - highestOctave);
		maxHarmonic = maxHarmonic * 2.pow(maxOctave - highestOctave);
	});

	if (maxFreq.log2 > 7, {
		modHelper = modHelper * 10;
	});

	// Allow more sparkle on top when available
	if ((maxHarmonic *3)< 20000, { maxHarmonic = maxHarmonic * 2 });

	name = name ? "fm_synth";

	SynthDef(name, { arg out = 0,
		freq=300, amp=0.1, dur=4, // basic music playback args
		pan = 0, sus = 1, pfreq = 100, // positional and color args
		t = 0,  cpc = 4, phrase = 16; // phrasing parameters for functions of time

		// The final outputzz signal, starts with nothing.
		var sig = 0;

		// An array to hold the carriers as we make them
		var cars = [];

		// How far we are into the current measure of music
		var cellProgress = t.mod(cpc)/cpc;

		// When looping bars of music indefinitely,
		// reset the param automation for each phrase
		var phraseProgress = t.mod(phrase)/phrase;


		// Create a glide using the previous frequency (pfreq)
		freq = Line.ar(pfreq, freq, dur*glide);


		// call a callback function to generate carriers
		cars = nCarriers.collect({|j|
			// The audio signal for this carrier, starts with nothing.
			var snd = 0;

			// The modulation signal for this carrier.
			var mod = 0;

			// Give this carrier a unique amplitude envelope
			var env = [Env.perc, Env.linen, Env.sine, Env.triangle].choose;

			// Some random attack and release values
			var atk = dur * 2.pow((-8..-2).choose);
			var rel = dur * 2.pow((-2..2).choose);

			// Construct the envelope
			var ampEnv = amp * EnvGen.ar(env.value(attackTime: dur/8, releaseTime: rel), timeScale: sus * (0.2 + 0.7.rand));

			// A dynamic control for the modulator
			// The visible portion of modulation increases as we get to the end of the phrase
			var timbreMix = LFSaw.ar((cps * phrase).reciprocal, pi * phraseProgress).range(0, 1); // share the timbre mix across all formants

			// detune the frequency using optimal values, with a higher tone in one channel and lower in the other
			var detuned = freq * [2.pow(j)*root, 2.pow(j)*root.neg].scramble;

			modsPerCarrier.do{|rate, i|
				var modMix, modIndex, modulator, timbre, env, modEnv;

				env = [Env.perc, Env.linen, Env.sine, Env.triangle].choose;
				// Generate an env sustain value based on index. First one has the most influence, the last has the least
				modEnv = EnvGen.ar(env, timeScale: sus * 2 / (i + 1));

				// Generate a mix value based on index. First one has the most influence, the last has the least
				modMix = (modsPerCarrier - i)/modsPerCarrier;

				// Control the amplitude of sidebands with some dynamic params
				modIndex = timbreMix * modMix * modEnv;
				// modIndex = 1 * modMix * modEnv;


				// pick a modulation source
				modulator = modulators.wrapAt(i);

				// create the modulator
				timbre = modHelper * modulator.ar(freq: modulationRatio * freq, mul: modulationRatio * modIndex);

				// add this modulation component to the modulation mixture
				mod = mod + timbre;
			};

			// return a modulated carrier wave to the cars array
			ampEnv * carrier.ar(freq + mod, mul: carScale);
		});


		// Mix em up
		sig = Mix.ar(cars);


		// Apply high pass and low pass filters to keep the sound in bounds
		sig = HPF.ar(sig, minHarmonic);
		sig = LPF.ar(sig, maxHarmonic);

		// Put the signal somewhere in space
		sig = Pan2.ar(sig, pan);

		Out.ar(0, sig);

		// Use doneAction: 2 to free the node when the amplitude drops to silence
		DetectSilence.ar(sig, time: dur/3, doneAction: 2);
	});
};

)