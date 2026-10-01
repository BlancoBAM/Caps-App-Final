# 🎙️ Caps App — Complete Audio Recording & Voice Generation Script

This document contains the complete script for all voice lines, letter pronunciations, vocabulary words, and encouraging character phrases for **Caps the Capybara**. 

You can use these scripts to record your own voice, hire a voice actor, or generate audio files using AI tools (such as **ElevenLabs**, **OpenAI TTS**, or **Cartesia**).

---

## 🛠️ Audio File Technical Specifications

- **Target Directory in Project**:  
  `easton-learning-app/app/src/main/res/raw/`
- **Supported Formats**:  
  `.mp3` (recommended) or `.ogg` / `.wav`
- **Audio Specs**:  
  44.1 kHz or 48 kHz, 16-bit, Mono or Stereo, trimmed closely at start and finish (no long silent pauses).
- **Naming Rule**:  
  Android resource names must use **lowercase letters, numbers, and underscores only** (no hyphens, spaces, or uppercase characters).
- **Automatic Fallback**:  
  The app automatically checks `res/raw/` for the matching filename. If a custom audio file is present, the app plays it immediately. If an audio file is missing, the app automatically falls back to clean Text-to-Speech!

### 🎭 Recommended Voice Persona (Caps the Capybara)
- **Tone**: Warm, cheerful, friendly, patient, and highly encouraging.
- **Pitch**: Slightly elevated/bright (appealing and clear for young children).
- **Cadence**: Clean, unhurried articulation so early readers can clearly distinguish consonant and vowel sounds.

---

## 📑 Table of Contents
1. [Alphabet Letters (A to Z)](#1-alphabet-letters-a-to-z)
2. [Level 1 Vocabulary (1 Syllable, 1–3 Letters)](#2-level-1-vocabulary-1-syllable-13-letters)
3. [Level 2 Vocabulary (1–2 Syllables, 4–5 Letters)](#3-level-2-vocabulary-12-syllables-45-letters)
4. [Level 3 Vocabulary (2–3 Syllables, 5–10 Letters)](#4-level-3-vocabulary-23-syllables-510-letters)
5. [Welcome, Onboarding & Navigation](#5-welcome-onboarding--navigation)
6. [Celebrations & Encouragement](#6-celebrations--encouragement)
7. [Gentle Hints & Mistake Support](#7-gentle-hints--mistake-support)

---

## 1. Alphabet Letters (A to Z)

These are used in **Learn By Listening** mode and when pronouncing individual letter presses.

| Filename | Letter | Letter Name & Sound Script | Phonics Association Script |
|:---|:---:|:---|:---|
| `letter_a.mp3` | **A** | "A! Ah!" | "A is for Apple! Apple starts with A!" |
| `letter_b.mp3` | **B** | "B! Buh!" | "B is for Bear! Big friendly bear!" |
| `letter_c.mp3` | **C** | "C! Cuh!" | "C is for Cat! Meow!" |
| `letter_d.mp3` | **D** | "D! Duh!" | "D is for Dog! Woof woof!" |
| `letter_e.mp3` | **E** | "E! Eh!" | "E is for Elephant! Giant elephant!" |
| `letter_f.mp3` | **F** | "F! Fff!" | "F is for Fish! Swimming in the sea!" |
| `letter_g.mp3` | **G** | "G! Guh!" | "G is for Giraffe! Tall neck giraffe!" |
| `letter_h.mp3` | **H** | "H! Huh!" | "H is for Hat! Put on your thinking hat!" |
| `letter_i.mp3` | **I** | "I! Ih!" | "I is for Ice Cream! Yummy treat!" |
| `letter_j.mp3` | **J** | "J! Juh!" | "J is for Jellyfish! Floating in the water!" |
| `letter_k.mp3` | **K** | "K! Kuh!" | "K is for Kangaroo! Hop hop hop!" |
| `letter_l.mp3` | **L** | "L! Lll!" | "L is for Lion! King of the jungle!" |
| `letter_m.mp3` | **M** | "M! Mmm!" | "M is for Monkey! Eating a banana!" |
| `letter_n.mp3` | **N** | "N! Nnn!" | "N is for Nest! Cozy bird home!" |
| `letter_o.mp3` | **O** | "O! Ah!" | "O is for Orange! Sweet citrus fruit!" |
| `letter_p.mp3` | **P** | "P! Puh!" | "P is for Panda! Munching on bamboo!" |
| `letter_q.mp3` | **Q** | "Q! Kwuh!" | "Q is for Queen! Wearing a golden crown!" |
| `letter_r.mp3` | **R** | "R! Rrr!" | "R is for Rainbow! Beautiful colors!" |
| `letter_s.mp3` | **S** | "S! Sss!" | "S is for Sun! Warm and bright!" |
| `letter_t.mp3` | **T** | "T! Tuh!" | "T is for Tiger! Striped and strong!" |
| `letter_u.mp3` | **U** | "U! Uh!" | "U is for Umbrella! Keeping dry in the rain!" |
| `letter_v.mp3` | **V** | "V! Vvv!" | "V is for Violin! Playing sweet music!" |
| `letter_w.mp3` | **W** | "W! Wuh!" | "W is for Watermelon! Fresh and juicy!" |
| `letter_x.mp3` | **X** | "X! Ecks!" | "X is for Xylophone! Making happy sounds!" |
| `letter_y.mp3` | **Y** | "Y! Yuh!" | "Y is for Yo-yo! Spinning up and down!" |
| `letter_z.mp3` | **Z** | "Z! Zzz!" | "Z is for Zebra! Black and white stripes!" |

---

## 2. Level 1 Vocabulary (1 Syllable, 1–3 Letters)

Targeted at preschool and early kindergarten phonics readers.

| Filename | Word | Word Prompt Script | Spelling Guide Script |
|:---|:---|:---|:---|
| `word_a.mp3` | **a** | "Can you spell the word: A?" | "The word A is spelled A!" |
| `word_i.mp3` | **i** | "Can you spell the word: I?" | "The word I is spelled I!" |
| `word_go.mp3` | **go** | "Can you spell: Go?" | "Go is spelled G - O!" |
| `word_no.mp3` | **no** | "Can you spell: No?" | "No is spelled N - O!" |
| `word_up.mp3` | **up** | "Can you spell: Up?" | "Up is spelled U - P!" |
| `word_me.mp3` | **me** | "Can you spell: Me?" | "Me is spelled M - E!" |
| `word_we.mp3` | **we** | "Can you spell: We?" | "We is spelled W - E!" |
| `word_he.mp3` | **he** | "Can you spell: He?" | "He is spelled H - E!" |
| `word_be.mp3` | **be** | "Can you spell: Be?" | "Be is spelled B - E!" |
| `word_to.mp3` | **to** | "Can you spell: To?" | "To is spelled T - O!" |
| `word_do.mp3` | **do** | "Can you spell: Do?" | "Do is spelled D - O!" |
| `word_it.mp3` | **it** | "Can you spell: It?" | "It is spelled I - T!" |
| `word_at.mp3` | **at** | "Can you spell: At?" | "At is spelled A - T!" |
| `word_on.mp3` | **on** | "Can you spell: On?" | "On is spelled O - N!" |
| `word_in.mp3` | **in** | "Can you spell: In?" | "In is spelled I - N!" |
| `word_is.mp3` | **is** | "Can you spell: Is?" | "Is is spelled I - S!" |
| `word_am.mp3` | **am** | "Can you spell: Am?" | "Am is spelled A - M!" |
| `word_or.mp3` | **or** | "Can you spell: Or?" | "Or is spelled O - R!" |
| `word_so.mp3` | **so** | "Can you spell: So?" | "So is spelled S - O!" |
| `word_my.mp3` | **my** | "Can you spell: My?" | "My is spelled M - Y!" |
| `word_by.mp3` | **by** | "Can you spell: By?" | "By is spelled B - Y!" |
| `word_cat.mp3` | **cat** | "Can you spell: Cat?" | "Cat is spelled C - A - T!" |
| `word_dog.mp3` | **dog** | "Can you spell: Dog?" | "Dog is spelled D - O - G!" |
| `word_sun.mp3` | **sun** | "Can you spell: Sun?" | "Sun is spelled S - U - N!" |
| `word_hat.mp3` | **hat** | "Can you spell: Hat?" | "Hat is spelled H - A - T!" |
| `word_pig.mp3` | **pig** | "Can you spell: Pig?" | "Pig is spelled P - I - G!" |
| `word_fox.mp3` | **fox** | "Can you spell: Fox?" | "Fox is spelled F - O - X!" |
| `word_bed.mp3` | **bed** | "Can you spell: Bed?" | "Bed is spelled B - E - D!" |
| `word_cup.mp3` | **cup** | "Can you spell: Cup?" | "Cup is spelled C - U - P!" |
| `word_car.mp3` | **car** | "Can you spell: Car?" | "Car is spelled C - A - R!" |
| `word_bus.mp3` | **bus** | "Can you spell: Bus?" | "Bus is spelled B - U - S!" |
| `word_run.mp3` | **run** | "Can you spell: Run?" | "Run is spelled R - U - N!" |
| `word_hop.mp3` | **hop** | "Can you spell: Hop?" | "Hop is spelled H - O - P!" |
| `word_red.mp3` | **red** | "Can you spell: Red?" | "Red is spelled R - E - D!" |
| `word_big.mp3` | **big** | "Can you spell: Big?" | "Big is spelled B - I - G!" |
| `word_fun.mp3` | **fun** | "Can you spell: Fun?" | "Fun is spelled F - U - N!" |
| `word_bee.mp3` | **bee** | "Can you spell: Bee?" | "Bee is spelled B - E - E!" |
| `word_bat.mp3` | **bat** | "Can you spell: Bat?" | "Bat is spelled B - A - T!" |
| `word_cow.mp3` | **cow** | "Can you spell: Cow?" | "Cow is spelled C - O - W!" |
| `word_box.mp3` | **box** | "Can you spell: Box?" | "Box is spelled B - O - X!" |
| `word_toy.mp3` | **toy** | "Can you spell: Toy?" | "Toy is spelled T - O - Y!" |
| `word_pen.mp3` | **pen** | "Can you spell: Pen?" | "Pen is spelled P - E - N!" |
| `word_ice.mp3` | **ice** | "Can you spell: Ice?" | "Ice is spelled I - C - E!" |
| `word_jam.mp3` | **jam** | "Can you spell: Jam?" | "Jam is spelled J - A - M!" |
| `word_pie.mp3` | **pie** | "Can you spell: Pie?" | "Pie is spelled P - I - E!" |
| `word_hen.mp3` | **hen** | "Can you spell: Hen?" | "Hen is spelled H - E - N!" |
| `word_ant.mp3` | **ant** | "Can you spell: Ant?" | "Ant is spelled A - N - T!" |
| `word_owl.mp3` | **owl** | "Can you spell: Owl?" | "Owl is spelled O - W - L!" |
| `word_egg.mp3` | **egg** | "Can you spell: Egg?" | "Egg is spelled E - G - G!" |
| `word_nut.mp3` | **nut** | "Can you spell: Nut?" | "Nut is spelled N - U - T!" |
| `word_bag.mp3` | **bag** | "Can you spell: Bag?" | "Bag is spelled B - A - G!" |
| `word_web.mp3` | **web** | "Can you spell: Web?" | "Web is spelled W - E - B!" |
| `word_map.mp3` | **map** | "Can you spell: Map?" | "Map is spelled M - A - P!" |
| `word_sit.mp3` | **sit** | "Can you spell: Sit?" | "Sit is spelled S - I - T!" |
| `word_hit.mp3` | **hit** | "Can you spell: Hit?" | "Hit is spelled H - I - T!" |
| `word_pot.mp3` | **pot** | "Can you spell: Pot?" | "Pot is spelled P - O - T!" |
| `word_top.mp3` | **top** | "Can you spell: Top?" | "Top is spelled T - O - P!" |
| `word_van.mp3` | **van** | "Can you spell: Van?" | "Van is spelled V - A - N!" |
| `word_rug.mp3` | **rug** | "Can you spell: Rug?" | "Rug is spelled R - U - G!" |
| `word_bug.mp3` | **bug** | "Can you spell: Bug?" | "Bug is spelled B - U - G!" |
| `word_log.mp3` | **log** | "Can you spell: Log?" | "Log is spelled L - O - G!" |
| `word_mud.mp3` | **mud** | "Can you spell: Mud?" | "Mud is spelled M - U - D!" |
| `word_net.mp3` | **net** | "Can you spell: Net?" | "Net is spelled N - E - T!" |
| `word_lip.mp3` | **lip** | "Can you spell: Lip?" | "Lip is spelled L - I - P!" |
| `word_tub.mp3` | **tub** | "Can you spell: Tub?" | "Tub is spelled T - U - B!" |

---

## 3. Level 2 Vocabulary (1–2 Syllables, 4–5 Letters)

Blends, consonant digraphs, and everyday two-syllable words.

| Filename | Word | Word Prompt Script | Spelling Guide Script |
|:---|:---|:---|:---|
| `word_frog.mp3` | **frog** | "Can you spell: Frog?" | "Frog is spelled F - R - O - G!" |
| `word_duck.mp3` | **duck** | "Can you spell: Duck?" | "Duck is spelled D - U - C - K!" |
| `word_bear.mp3` | **bear** | "Can you spell: Bear?" | "Bear is spelled B - E - A - R!" |
| `word_fish.mp3` | **fish** | "Can you spell: Fish?" | "Fish is spelled F - I - S - H!" |
| `word_star.mp3` | **star** | "Can you spell: Star?" | "Star is spelled S - T - A - R!" |
| `word_tree.mp3` | **tree** | "Can you spell: Tree?" | "Tree is spelled T - R - E - E!" |
| `word_bird.mp3` | **bird** | "Can you spell: Bird?" | "Bird is spelled B - I - R - D!" |
| `word_lion.mp3` | **lion** | "Can you spell: Lion?" | "Lion is spelled L - I - O - N!" |
| `word_cake.mp3` | **cake** | "Can you spell: Cake?" | "Cake is spelled C - A - K - E!" |
| `word_boat.mp3` | **boat** | "Can you spell: Boat?" | "Boat is spelled B - O - A - T!" |
| `word_moon.mp3` | **moon** | "Can you spell: Moon?" | "Moon is spelled M - O - O - N!" |
| `word_book.mp3` | **book** | "Can you spell: Book?" | "Book is spelled B - O - O - K!" |
| `word_ball.mp3` | **ball** | "Can you spell: Ball?" | "Ball is spelled B - A - L - L!" |
| `word_door.mp3` | **door** | "Can you spell: Door?" | "Door is spelled D - O - O - R!" |
| `word_lamp.mp3` | **lamp** | "Can you spell: Lamp?" | "Lamp is spelled L - A - M - P!" |
| `word_milk.mp3` | **milk** | "Can you spell: Milk?" | "Milk is spelled M - I - L - K!" |
| `word_nest.mp3` | **nest** | "Can you spell: Nest?" | "Nest is spelled N - E - S - T!" |
| `word_park.mp3` | **park** | "Can you spell: Park?" | "Park is spelled P - A - R - K!" |
| `word_ring.mp3` | **ring** | "Can you spell: Ring?" | "Ring is spelled R - I - N - G!" |
| `word_shoe.mp3` | **shoe** | "Can you spell: Shoe?" | "Shoe is spelled S - H - O - E!" |
| `word_wind.mp3` | **wind** | "Can you spell: Wind?" | "Wind is spelled W - I - N - D!" |
| `word_leaf.mp3` | **leaf** | "Can you spell: Leaf?" | "Leaf is spelled L - E - A - F!" |
| `word_ship.mp3` | **ship** | "Can you spell: Ship?" | "Ship is spelled S - H - I - P!" |
| `word_bell.mp3` | **bell** | "Can you spell: Bell?" | "Bell is spelled B - E - L - L!" |
| `word_drum.mp3` | **drum** | "Can you spell: Drum?" | "Drum is spelled D - R - U - M!" |
| `word_flag.mp3` | **flag** | "Can you spell: Flag?" | "Flag is spelled F - L - A - G!" |
| `word_hand.mp3` | **hand** | "Can you spell: Hand?" | "Hand is spelled H - A - N - D!" |
| `word_jump.mp3` | **jump** | "Can you spell: Jump?" | "Jump is spelled J - U - M - P!" |
| `word_king.mp3` | **king** | "Can you spell: King?" | "King is spelled K - I - N - G!" |
| `word_play.mp3` | **play** | "Can you spell: Play?" | "Play is spelled P - L - A - Y!" |
| `word_rain.mp3` | **rain** | "Can you spell: Rain?" | "Rain is spelled R - A - I - N!" |
| `word_snow.mp3` | **snow** | "Can you spell: Snow?" | "Snow is spelled S - N - O - W!" |
| `word_swim.mp3` | **swim** | "Can you spell: Swim?" | "Swim is spelled S - W - I - M!" |
| `word_kite.mp3` | **kite** | "Can you spell: Kite?" | "Kite is spelled K - I - T - E!" |
| `word_baby.mp3` | **baby** | "Can you spell: Baby?" | "Baby is spelled B - A - B - Y!" |
| `word_water.mp3` | **water** | "Can you spell: Water?" | "Water is spelled W - A - T - E - R!" |
| `word_apple.mp3` | **apple** | "Can you spell: Apple?" | "Apple is spelled A - P - P - L - E!" |
| `word_puppy.mp3` | **puppy** | "Can you spell: Puppy?" | "Puppy is spelled P - U - P - P - Y!" |
| `word_kitty.mp3` | **kitty** | "Can you spell: Kitty?" | "Kitty is spelled K - I - T - T - Y!" |
| `word_happy.mp3` | **happy** | "Can you spell: Happy?" | "Happy is spelled H - A - P - P - Y!" |
| `word_panda.mp3` | **panda** | "Can you spell: Panda?" | "Panda is spelled P - A - N - D - A!" |
| `word_tiger.mp3` | **tiger** | "Can you spell: Tiger?" | "Tiger is spelled T - I - G - E - R!" |
| `word_zebra.mp3` | **zebra** | "Can you spell: Zebra?" | "Zebra is spelled Z - E - B - R - A!" |
| `word_pizza.mp3` | **pizza** | "Can you spell: Pizza?" | "Pizza is spelled P - I - Z - Z - A!" |
| `word_bunny.mp3` | **bunny** | "Can you spell: Bunny?" | "Bunny is spelled B - U - N - N - Y!" |
| `word_sunny.mp3` | **sunny** | "Can you spell: Sunny?" | "Sunny is spelled S - U - N - N - Y!" |
| `word_candy.mp3` | **candy** | "Can you spell: Candy?" | "Candy is spelled C - A - N - D - Y!" |
| `word_cookie.mp3` | **cookie** | "Can you spell: Cookie?" | "Cookie is spelled C - O - O - K - I - E!" |
| `word_funny.mp3` | **funny** | "Can you spell: Funny?" | "Funny is spelled F - U - N - N - Y!" |
| `word_magic.mp3` | **magic** | "Can you spell: Magic?" | "Magic is spelled M - A - G - I - C!" |
| `word_music.mp3` | **music** | "Can you spell: Music?" | "Music is spelled M - U - S - I - C!" |
| `word_robot.mp3` | **robot** | "Can you spell: Robot?" | "Robot is spelled R - O - B - O - T!" |
| `word_cloudy.mp3` | **cloudy** | "Can you spell: Cloudy?" | "Cloudy is spelled C - L - O - U - D - Y!" |
| `word_picnic.mp3` | **picnic** | "Can you spell: Picnic?" | "Picnic is spelled P - I - C - N - I - C!" |
| `word_monkey.mp3` | **monkey** | "Can you spell: Monkey?" | "Monkey is spelled M - O - N - K - E - Y!" |
| `word_pencil.mp3` | **pencil** | "Can you spell: Pencil?" | "Pencil is spelled P - E - N - C - I - L!" |
| `word_pocket.mp3` | **pocket** | "Can you spell: Pocket?" | "Pocket is spelled P - O - C - K - E - T!" |
| `word_yellow.mp3` | **yellow** | "Can you spell: Yellow?" | "Yellow is spelled Y - E - L - L - O - W!" |
| `word_purple.mp3` | **purple** | "Can you spell: Purple?" | "Purple is spelled P - U - R - P - L - E!" |
| `word_orange.mp3` | **orange** | "Can you spell: Orange?" | "Orange is spelled O - R - A - N - G - E!" |
| `word_flower.mp3` | **flower** | "Can you spell: Flower?" | "Flower is spelled F - L - O - W - E - R!" |
| `word_garden.mp3` | **garden** | "Can you spell: Garden?" | "Garden is spelled G - A - R - D - E - N!" |
| `word_castle.mp3` | **castle** | "Can you spell: Castle?" | "Castle is spelled C - A - S - T - L - E!" |
| `word_rocket.mp3` | **rocket** | "Can you spell: Rocket?" | "Rocket is spelled R - O - C - K - E - T!" |

---

## 4. Level 3 Vocabulary (2–3 Syllables, 5–10 Letters)

Multi-syllable challenge words for developing spelling mastery.

| Filename | Word | Word Prompt Script | Spelling Guide Script |
|:---|:---|:---|:---|
| `word_rainbow.mp3` | **rainbow** | "Can you spell: Rainbow?" | "Rainbow is spelled R - A - I - N - B - O - W!" |
| `word_sunshine.mp3` | **sunshine** | "Can you spell: Sunshine?" | "Sunshine is spelled S - U - N - S - H - I - N - E!" |
| `word_dolphin.mp3` | **dolphin** | "Can you spell: Dolphin?" | "Dolphin is spelled D - O - L - P - H - I - N!" |
| `word_feather.mp3` | **feather** | "Can you spell: Feather?" | "Feather is spelled F - E - A - T - H - E - R!" |
| `word_window.mp3` | **window** | "Can you spell: Window?" | "Window is spelled W - I - N - D - O - W!" |
| `word_blanket.mp3` | **blanket** | "Can you spell: Blanket?" | "Blanket is spelled B - L - A - N - K - E - T!" |
| `word_butterfly.mp3` | **butterfly** | "Can you spell: Butterfly?" | "Butterfly is spelled B - U - T - T - E - R - F - L - Y!" |
| `word_banana.mp3` | **banana** | "Can you spell: Banana?" | "Banana is spelled B - A - N - A - N - A!" |
| `word_elephant.mp3` | **elephant** | "Can you spell: Elephant?" | "Elephant is spelled E - L - E - P - H - A - N - T!" |
| `word_dinosaur.mp3` | **dinosaur** | "Can you spell: Dinosaur?" | "Dinosaur is spelled D - I - N - O - S - A - U - R!" |
| `word_umbrella.mp3` | **umbrella** | "Can you spell: Umbrella?" | "Umbrella is spelled U - M - B - R - E - L - L - A!" |
| `word_sunflower.mp3` | **sunflower** | "Can you spell: Sunflower?" | "Sunflower is spelled S - U - N - F - L - O - W - E - R!" |
| `word_caterpillar.mp3` | **caterpillar** | "Can you spell: Caterpillar?" | "Caterpillar is spelled C - A - T - E - R - P - I - L - L - A - R!" |
| `word_hospital.mp3` | **hospital** | "Can you spell: Hospital?" | "Hospital is spelled H - O - S - P - I - T - A - L!" |
| `word_computer.mp3` | **computer** | "Can you spell: Computer?" | "Computer is spelled C - O - M - P - U - T - E - R!" |
| `word_adventure.mp3` | **adventure** | "Can you spell: Adventure?" | "Adventure is spelled A - D - V - E - N - T - U - R - E!" |
| `word_together.mp3` | **together** | "Can you spell: Together?" | "Together is spelled T - O - G - E - T - H - E - R!" |
| `word_wonderful.mp3` | **wonderful** | "Can you spell: Wonderful?" | "Wonderful is spelled W - O - N - D - E - R - F - U - L!" |
| `word_capybara.mp3` | **capybara** | "Can you spell my name: Capybara?" | "Capybara is spelled C - A - P - Y - B - A - R - A!" |
| `word_fantastic.mp3` | **fantastic** | "Can you spell: Fantastic?" | "Fantastic is spelled F - A - N - T - A - S - T - I - C!" |
| `word_alphabet.mp3` | **alphabet** | "Can you spell: Alphabet?" | "Alphabet is spelled A - L - P - H - A - B - E - T!" |
| `word_pineapple.mp3` | **pineapple** | "Can you spell: Pineapple?" | "Pineapple is spelled P - I - N - E - A - P - P - L - E!" |
| `word_watermelon.mp3` | **watermelon** | "Can you spell: Watermelon?" | "Watermelon is spelled W - A - T - E - R - M - E - L - O - N!" |
| `word_chocolate.mp3` | **chocolate** | "Can you spell: Chocolate?" | "Chocolate is spelled C - H - O - C - O - L - A - T - E!" |
| `word_instrument.mp3` | **instrument** | "Can you spell: Instrument?" | "Instrument is spelled I - N - S - T - R - U - M - E - N - T!" |
| `word_tomorrow.mp3` | **tomorrow** | "Can you spell: Tomorrow?" | "Tomorrow is spelled T - O - M - O - R - R - O - W!" |
| `word_beautiful.mp3` | **beautiful** | "Can you spell: Beautiful?" | "Beautiful is spelled B - E - A - U - T - I - F - U - L!" |
| `word_lemonade.mp3` | **lemonade** | "Can you spell: Lemonade?" | "Lemonade is spelled L - E - M - O - N - A - D - E!" |
| `word_basketball.mp3` | **basketball** | "Can you spell: Basketball?" | "Basketball is spelled B - A - S - K - E - T - B - A - L - L!" |
| `word_marshmallow.mp3` | **marshmallow** | "Can you spell: Marshmallow?" | "Marshmallow is spelled M - A - R - S - H - M - A - L - L - O - W!" |
| `word_octopus.mp3` | **octopus** | "Can you spell: Octopus?" | "Octopus is spelled O - C - T - O - P - U - S!" |
| `word_helicopter.mp3` | **helicopter** | "Can you spell: Helicopter?" | "Helicopter is spelled H - E - L - I - C - O - P - T - E - R!" |
| `word_astronaut.mp3` | **astronaut** | "Can you spell: Astronaut?" | "Astronaut is spelled A - S - T - R - O - N - A - U - T!" |
| `word_telescope.mp3` | **telescope** | "Can you spell: Telescope?" | "Telescope is spelled T - E - L - E - S - C - O - P - E!" |

---

## 5. Welcome, Onboarding & Navigation

| Filename | Trigger | Voice Script |
|:---|:---|:---|
| `welcome_first.mp3` | First launch | "Hi there! I'm Caps the Capybara! What's your name?" |
| `ask_name.mp3` | Name input prompt | "Please type your name so we can learn together!" |
| `welcome_confirmed.mp3` | Name submitted | "Awesome to meet you! Let's have some fun!" |
| `welcome_back.mp3` | Returning app launch | "Welcome back! Choose how you'd like to learn today!" |
| `menu_greeting.mp3` | Main menu opened | "Choose how you'd like to learn today! Touch Learn by Listening or Spelling Bee!" |
| `lbl_intro.mp3` | Learn By Listening started | "Touch any letter on screen or press a key on your keyboard to hear its sound!" |
| `nav_level_switch.mp3` | Level selector clicked | "Level updated! Let's try some new words!" |

---

## 6. Celebrations & Encouragement

| Filename | Trigger | Voice Script |
|:---|:---|:---|
| `cheer_awesome.mp3` | Word completed | "Awesome job! You spelled that word perfectly!" |
| `cheer_superstar.mp3` | Word completed | "SUPERSTAR! You're getting so good at spelling!" |
| `cheer_fantastic.mp3` | Word completed | "Fantastic spelling! Give yourself a high five!" |
| `cheer_brilliant.mp3` | Word completed | "Brilliant work! Let's try another one!" |
| `level_up_2.mp3` | Promoted to Level 2 | "You're doing amazing! Moving to Level 2 with bigger words!" |
| `level_up_3.mp3` | Promoted to Level 3 | "SUPERSTAR! You reached Level 3! Let's spell like a champ!" |

---

## 7. Gentle Hints & Mistake Support

Positive reinforcement without frustration.

| Filename | Letter Hinted | Voice Script |
|:---|:---:|:---|
| `hint_a.mp3` | **A** | "Almost! Try pressing the letter A!" |
| `hint_b.mp3` | **B** | "Nice try! Look for the letter B!" |
| `hint_c.mp3` | **C** | "Good try! Next letter is C!" |
| `hint_d.mp3` | **D** | "Almost! Try pressing the letter D!" |
| `hint_e.mp3` | **E** | "You're close! Look for the letter E!" |
| `hint_f.mp3` | **F** | "Nice try! Look for the letter F!" |
| `hint_g.mp3` | **G** | "Good effort! Next letter is G!" |
| `hint_h.mp3` | **H** | "Almost! Try pressing the letter H!" |
| `hint_i.mp3` | **I** | "Good try! Next letter is I!" |
| `hint_j.mp3` | **J** | "Look for the letter J!" |
| `hint_k.mp3` | **K** | "You've got this! Look for the letter K!" |
| `hint_l.mp3` | **L** | "Almost! Try pressing L!" |
| `hint_m.mp3` | **M** | "Nice try! Look for the letter M!" |
| `hint_n.mp3` | **N** | "Good try! Next letter is N!" |
| `hint_o.mp3` | **O** | "Almost! Try pressing O!" |
| `hint_p.mp3` | **P** | "You're close! Look for the letter P!" |
| `hint_q.mp3` | **Q** | "Look for the letter Q!" |
| `hint_r.mp3` | **R** | "Good try! Look for the letter R!" |
| `hint_s.mp3` | **S** | "Almost! Try pressing S!" |
| `hint_t.mp3` | **T** | "You're close! Next letter is T!" |
| `hint_u.mp3` | **U** | "Good effort! Look for the letter U!" |
| `hint_v.mp3` | **V** | "Look for the letter V!" |
| `hint_w.mp3` | **W** | "Almost! Try pressing W!" |
| `hint_x.mp3` | **X** | "Look for the letter X!" |
| `hint_y.mp3` | **Y** | "Good try! Look for the letter Y!" |
| `hint_z.mp3` | **Z** | "Almost! Look for the letter Z!" |
| `hint_general_retry.mp3` | General error | "Take your time, friend! You can do it!" |
