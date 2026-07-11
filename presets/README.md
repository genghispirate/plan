# 🎞️ Film Look Presets for Lightroom Mobile

A small collection of `.xmp` presets that emulate classic film stocks — lifted
"matte" blacks, warm skin, muted greens, split-toned shadows/highlights, and
real grain. They work in **Lightroom Mobile (iOS/Android)**, Lightroom CC
desktop, and Lightroom Classic.

| File | Preset name | Group | Best for |
|------|-------------|-------|----------|
| `00-Portrait-Mediterranean-Man.xmp` | **Portrait – Mediterranean Man** | Portrait | Olive/warm skin, bald head, dark beard |
| `01-Film-Portra-Warm.xmp` | **Film – Portra Warm** | Film Look | Everyday, portraits, skin tones (Kodak Portra vibe) |
| `02-Film-Cinematic-Teal-Orange.xmp` | **Film – Cinematic Teal & Orange** | Film Look | Moody portraits, street, **video** |
| `03-Film-Faded-Matte.xmp` | **Film – Faded Matte** | Film Look | Travel, lifestyle, golden hour, dreamy pastels |
| `04-Film-BW-Classic.xmp` | **Film – B&W Classic** | Film Look | Black & white, high-contrast Tri-X style |

The film presets share the group **"Film Look"**; the portrait preset sits in a
**"Portrait"** group. They'll appear grouped in Lightroom's preset panel.

> **File format note:** these are built to match a real Adobe-exported preset
> exactly — `crs:ProcessVersion="11.0"`, `crs:SupportsAmount="False"`, and the
> full `crs:` default attribute set — so they import cleanly into Lightroom
> Classic, Lightroom CC, and Lightroom Mobile without the "invalid preset" error.

---

## 📲 How to install on Lightroom Mobile

**Android — import the ZIP (most reliable):**

Android's file picker often hands Lightroom a loose `.xmp` as an unrecognized
type, causing an **"Unsupported file"** error. Importing the **`.zip`** avoids
this entirely.

1. Save **`Film-and-Portrait-Presets.zip`** to your phone (e.g. Downloads).
   Do **not** unzip it.
2. Open **Lightroom** → open any photo → tap **Edit**.
3. Tap **Presets** → the **`⋮`** (three dots) → **Import Presets**.
4. Browse to and select the **`.zip`**. All five presets import at once into
   their **Film Look** / **Portrait** groups.

**iPhone — import the `.xmp` files directly (free plan works):**

1. Save the `.xmp` files to Files.
2. Lightroom → open a photo → **Edit → Presets → `⋯` → Import Presets**.
3. Select the `.xmp` files (or the `.zip`).

**Alternative — sync from desktop:** drop the files into Lightroom CC on
desktop (File → Import Profiles & Presets), and they sync to mobile
automatically if you're signed into the same account.

> Tip: On some Android builds the "Import Presets" button only accepts one file
> at a time — just repeat step 4 for each.

---

## 🎥 Using these on video

Lightroom Mobile can edit video clips too. Open a clip and apply the preset the
same way. **Only a subset of settings transfer to video**, so results differ
slightly from photos:

- ✅ **Applies to video:** White balance, Exposure/Contrast/Highlights/Shadows/
  Whites/Blacks, Tone Curve, Vibrance/Saturation, HSL color mixer, Color Grading
  / Split Toning.
- ❌ **Ignored on video:** Grain, Texture, Clarity, Dehaze, Vignette, Sharpening.

**`02 – Cinematic Teal & Orange` is the best pick for video** because its look
lives mostly in the tone curve + color grade (which video honors), not in grain.
For a consistent clip, apply it to the first frame and, if the app offers it,
"copy settings → paste to all."

---

## 🎛️ Manual settings (dial them in by hand)

If you'd rather build the look yourself, here are the values. Panels follow the
Lightroom Mobile edit order.

### 00 · Portrait – Mediterranean Man

Tuned for a man with olive/Mediterranean warm-undertone skin, a shaved/bald head
that catches light, dark beard, and hooded eyes. Warms skin naturally, tames head
highlights, opens the eye area, and adds jaw/beard definition.

| Panel | Setting | Value |
|-------|---------|-------|
| **White Balance** | Temp / Tint | +8 / +4 (relative) |
| **Light** | Exposure | 0 |
| | Contrast | +12 |
| | Highlights | −35 |
| | Shadows | +20 |
| | Whites | +10 |
| | Blacks | −12 |
| **Effects** | Texture | +10 |
| | Clarity | +10 |
| | Dehaze | +5 |
| **Color → Mix (HSL)** | Orange lum / sat | +12 / −8 |
| | Red lum / sat | +5 / −5 |
| **Color Grading** | Shadows | Hue 210, Sat 8 |
| | Highlights | Hue 40, Sat 6 |
| | Blending / Balance | 50 / 0 |
| **Detail** | Sharpening | 40 |
| | Noise Reduction (Luminance) | 18 |

*Why these work:* Highlights −35 with Whites +10 tames the shiny scalp without
dulling the image; Orange luminance +12 / saturation −8 brightens and de-oranges
skin; the teal-blue shadow / warm-orange highlight split adds subtle depth and
warmth; Texture + Clarity +10 sharpen the beard and jawline; and the WB nudge
adds a natural Mediterranean warmth.

### 01 · Film – Portra Warm

| Panel | Setting | Value |
|-------|---------|-------|
| **Light** | Contrast | −10 |
| | Highlights | −20 |
| | Shadows | +25 |
| | Whites | −10 |
| | Blacks | +12 |
| **Tone Curve** (main) | Points | (0,22) (64,60) (128,128) (192,196) (255,244) |
| | Red channel | (0,10) (255,250) |
| | Green channel | (0,6) (255,252) |
| | Blue channel | (0,20) (128,124) (255,232) |
| **Color** | Vibrance | +12 |
| | Saturation | −6 |
| **Color → Mix (HSL)** | Green sat / lum | −22 / +10 |
| | Yellow sat | −14 |
| | Orange lum | +8 |
| | Blue sat / lum | −10 / −6 |
| | Hue: Yellow −12, Green −20 | |
| **Color Grading** | Shadows | Hue 200, Sat 12 |
| | Midtones | Hue 40, Sat 8 |
| | Highlights | Hue 48, Sat 10 |
| | Blending | 50 |
| **Effects** | Texture | +5 |
| | Clarity | −6 |
| | Dehaze | −4 |
| | Grain: Amount 22, Size 24, Roughness 50 | |
| | Vignette | −8 (feather 60) |

### 02 · Film – Cinematic Teal & Orange

| Panel | Setting | Value |
|-------|---------|-------|
| **Light** | Contrast +8 · Highlights −30 · Shadows +30 · Whites −6 · Blacks +8 | |
| **Tone Curve** | Main | (0,18) (64,52) (128,128) (192,205) (255,246) |
| | Blue channel | (0,24) (128,122) (255,226) |
| **Color** | Vibrance +6 · Saturation −10 | |
| **HSL** | Green sat −30 · Aqua sat −18 hue −15 · Orange lum +10 · Blue lum −12 | |
| **Color Grading** | Shadows Hue 195/Sat 18 · Mid Hue 30/Sat 6 · Highlights Hue 45/Sat 8 · Balance −10 · Blending 55 | |
| **Effects** | Texture +8 · Clarity −3 · Dehaze +3 · Grain 18/22/50 · Vignette −12 | |

### 03 · Film – Faded Matte

| Panel | Setting | Value |
|-------|---------|-------|
| **Light** | Contrast −18 · Highlights −10 · Shadows +30 · Whites −18 · Blacks +22 | |
| **Tone Curve** | Main | (0,32) (64,66) (128,124) (192,188) (255,232) |
| | Blue channel | (0,28) (128,126) (255,220) |
| **Color** | Vibrance +8 · Saturation −14 | |
| **HSL** | Green sat −26 lum +12 · Yellow sat −18 · Orange lum +10 | |
| **Color Grading** | Shadows Hue 210/Sat 14 · Highlights Hue 55/Sat 12 · Balance +10 · Blending 50 | |
| **Effects** | Texture −6 · Clarity −12 · Dehaze −8 · Grain 25/26/50 · Vignette −6 | |

### 04 · Film – B&W Classic

| Panel | Setting | Value |
|-------|---------|-------|
| **Profile** | Convert to Black & White | on |
| **Light** | Contrast +15 · Highlights −15 · Shadows +20 · Whites −5 · Blacks +6 | |
| **Tone Curve** | Main | (0,14) (64,54) (128,130) (192,204) (255,248) |
| **B&W Mix** | Red +20 · Orange +25 · Yellow +20 · Green −10 · Aqua −15 · Blue −25 · Purple −10 | |
| **Effects** | Texture +10 · Clarity +6 · Dehaze +4 · Grain 28/25/55 · Vignette −10 | |

---

## 🔧 Fine-tuning tips

- **Presets are a starting point.** After applying, always re-check **Exposure**
  and **White Balance / Temp** per photo — film looks depend on correct base
  exposure.
- **Too much fade?** Raise the bottom-left point of the main Tone Curve back
  down toward 0 to deepen blacks.
- **Skin too orange/green?** Nudge the **Orange** and **Green** HSL *Hue*
  sliders a few points.
- **Grain too heavy on small screens?** Grain scales with export size — drop
  Amount to ~12–15 for social.
- **Color cast on video looks strong?** Lower **Color Grading → Blending** or
  the shadow/highlight saturation by ~5.

Enjoy the grain. 🎞️
