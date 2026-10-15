"""
Blender / 3D Rigging & Animation Export Script for Caps the Capybara
Matches the Animation Pipeline Specification from blank.pdf:

- Foundational Visemes (Morph Targets / Shape Keys):
  1. mouth_rest_closed       (M, B, P)
  2. mouth_wide_open_ah      (Ah, A, O)
  3. mouth_round_puckered_oh (Oh/Oo, U, W)
  4. mouth_wide_smile_ee     (Ee, E, I, S, Z)
  5. mouth_teeth_on_lip_fv   (F, V)
  6. mouth_tongue_alveolar_ltd (L, T, D, N)

- Modular Animation Action Clips:
  - caps_idle.anim            (Gentle breathing, weight shift, blink)
  - caps_talk_neutral.anim    (Expressive hand talking gestures)
  - caps_praise.anim          (Clapping / rubbing hands, joyful bounce, wide smile)
  - caps_question.anim        (Head tilt, one hand raised / pointing, raised brow)
  - caps_gentle_guidance.anim (Reassuring open palms, comforting tilt)
"""

import sys

try:
    import bpy
    IN_BLENDER = True
except ImportError:
    IN_BLENDER = False

# List of required shape keys (visemes) for phoneme sync
REQUIRED_VISEMES = [
    ("mouth_rest_closed", "Neutral closed lips (M, B, P)"),
    ("mouth_wide_open_ah", "Low jaw drop for broad vowels (A, O)"),
    ("mouth_round_puckered_oh", "Tight rounded lips (U, W)"),
    ("mouth_wide_smile_ee", "Stretched lips (E, I, S, Z)"),
    ("mouth_teeth_on_lip_fv", "Upper teeth touching lower lip (F, V)"),
    ("mouth_tongue_alveolar_ltd", "Tongue tip hitting behind upper teeth (L, T, D)"),
]

# Action definitions with frame ranges and attributes
ACTION_CLIPS = [
    {
        "name": "caps_idle",
        "start": 1,
        "end": 60,
        "loop": True,
        "description": "Gentle breathing, subtle weight shift, relaxed clasped hands"
    },
    {
        "name": "caps_talk_neutral",
        "start": 61,
        "end": 120,
        "loop": True,
        "description": "Expressive hand talking gestures, open gesturing palms"
    },
    {
        "name": "caps_praise",
        "start": 121,
        "end": 180,
        "loop": False,
        "description": "Clapping/rubbing hands (sample clip gesture), joyful body bounce, wide smile"
    },
    {
        "name": "caps_question",
        "start": 181,
        "end": 240,
        "loop": False,
        "description": "Head tilt, right hand up/pointing, raised eyebrow"
    },
    {
        "name": "caps_gentle_guidance",
        "start": 241,
        "end": 300,
        "loop": False,
        "description": "Reassuring open palms, comforting slight tilt, sympathetic smile"
    }
]

def setup_viseme_shape_keys(mesh_obj):
    """Ensures foundational viseme shape keys exist on character head/face mesh."""
    if not IN_BLENDER:
        print("[Setup] Viseme morph targets defined:")
        for name, desc in REQUIRED_VISEMES:
            print(f"  - {name}: {desc}")
        return

    if not mesh_obj.data.shape_keys:
        mesh_obj.shape_key_add(name="Basis")

    existing = {sk.name for sk in mesh_obj.data.shape_keys.key_blocks}
    for name, desc in REQUIRED_VISEMES:
        if name not in existing:
            sk = mesh_obj.shape_key_add(name=name)
            sk.slider_min = 0.0
            sk.slider_max = 1.0
            print(f"Added shape key: {name} ({desc})")

def export_gltf_clips(output_path="caps_animations.glb"):
    """Exports all action clips and blend shapes into a unified glTF/GLB asset."""
    if not IN_BLENDER:
        print(f"[Export] glTF export target configured: {output_path}")
        print("Run within Blender via:")
        print(f"  blender character.blend --background --python export_caps_animations.py")
        return

    bpy.ops.export_scene.gltf(
        filepath=output_path,
        export_format='GLB',
        export_yup=True,
        export_apply=True,
        export_morph=True,
        export_morph_normal=True,
        export_animations=True,
        export_nla_strips=True,
        export_def_bones=True
    )
    print(f"Exported glTF asset to {output_path}")

if __name__ == "__main__":
    print("=== Caps Capybara 3D Animation Pipeline Tool ===")
    if IN_BLENDER:
        # Find active mesh object or character armature
        active = bpy.context.active_object
        if active and active.type == 'MESH':
            setup_viseme_shape_keys(active)
        export_gltf_clips()
    else:
        setup_viseme_shape_keys(None)
        export_gltf_clips()
