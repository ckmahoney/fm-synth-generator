# FM Synth Generator for SuperCollider

FM is fun-ky! You can get some wild sounds when you play with the modulation ratios and index.

This synth generator keeps things tame, but with enough modulation to add interesting color to the sound.

#### Start by opening global-function.sc to load the synth generator.
#### Then open player.sc to make some music!

- - - 


## quick FAQs about FM synthesis

### What is FM Synthesis?

FM Synthesis is the composition of complex waveforms by adding harmonics to a fundamental frequency from a modulation source.


### What are the key ingredients of FM synthesis?


Fundamental:

The note you want to play. When you sing, the notes you hear from your voice are the fundamental.


Harmonics:

 Frequencies that are an integer multiple of the fundamental. Ex, for a fundamental f, then f * 2, f * 3, f * 4, f / 2, f / 3, f / 4. When you sing and hold a tone while changing the shape of your mouth and tongue, you're changing the harmonics above the fundamental.


Sidebands:

The additional harmonics that appear above and below the fundamental because of modulation.



Modulation Ratio:

The multiplier used on the Modulator's input frequency. This affects the where the sidebands appear
(like translate-x in graphics).


Modulation Index: 

The amplitude multiplier for the modulation source. This affects how much you hear the sidebands.
(like translate-y in graphics, where y is the audio level of the sidebands).


Modulator:

A high frequency wave adding values to the carrier.


Carrier:

A high frequency wave being performed as an instrument.


## Synth Params

To make the instrument responsive, we have a few helpful synthdef params.


the  `t` parameter marks the current timestamp. Like time in real life, this is just a number that is constantly getting bigger. It is helpful to use a modulus operator on it to find out where you are in the current phrase. 


the `cpc` parameter describes how many Cycles Per Cell there are. "Cell" here is another word for measure of music. But Cells often last 8, 16, or 2 beats, whereas measures of music often last 3 or 4 beats.


the  `phrase` parameter to describe how many cycles go into a phrase. For example, a 4 bar phrase in 4/4 time is 16 cycles. So phrase=16.


the `root` parameter lets you change the key, so the synth can always know what key you're in


and `cps` tells you the tempo, so we can build beat-based LFOs into the instrument

In the given implementation we use those time params to control the amount of FM being applied to the instrument as a function of phrase.
so we have 0% modulation at the start of the phrase, and 100% modulation at the end of the phrase, increasing linearly with time.

There's plenty of places to poke around and wiggle the sound 
and i left a lot of comments hoping to make it easy to read and edit 
