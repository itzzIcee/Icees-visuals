# Icee's Visuals (Fabric, Minecraft 1.21.11)

Press **Right Shift** (in-game or on the title screen) to open the menu.

## Features
- **Themes**: create, switch and delete themes; set the menu accent color.
- **Textures**: swap any texture by entering its ID (e.g. `block/stone`) and a PNG path.
- **Sky**: 6 example skies (Purple Planet, Grey Smoke, Purple Nebula, Purple Marble, Storm, Fire).
  They replace the End sky texture.
- **Weapons**: animated skins (spinning butterfly knife, static butterfly knife, combat knife, glow sword)
  with color palettes and spin speed. They replace the model of the sword you choose.
- Click **Apply** to rebuild the resource pack `IceesVisuals` and reload textures.
  Click **Disable pack** to turn it off again.

Your data is stored in `config/iceesvisuals/`.

## Get the .jar (no installs needed): GitHub
1. Create a free account on github.com and make a new repository.
2. Upload ALL files from this zip (keep the folder structure, including `.github`).
3. Open the **Actions** tab, wait for "Build Icee's Visuals" to finish (about 3 minutes).
4. Open the finished run, download the **Icees-Visuals** artifact, unzip it.
5. Put `Icees-Visuals-1.0.0.jar` and **Fabric API** into `.minecraft/mods`.

## Build locally instead
Install JDK 21 and Gradle 9.2, open a terminal in this folder and run `gradle build`.
The jar is in `build/libs/`.

## Requirements
Minecraft 1.21.11, Fabric Loader 0.18.4+, Fabric API.
