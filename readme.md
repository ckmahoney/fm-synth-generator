# FM Synth Generator for SuperCollider

FM is fun-ky! You can get some wild sounds when you play with the modulation ratios and index.

This synth generator keeps things tame, but with enough modulation to add interesting color to the sound.

Start by opening global-function.sc to load the synth generator.
Then open player.sc to make some music!




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

Modulation Index: The amplitude multiplier for the modulation source. This affects how much you hear the sidebands.
(like translate-y in graphics, where y is the audio level of the sidebands).

Modulator:
A high frequency wave adding values to the carrier.

Carrier:
A high frequency wave being performed as an instrument.
