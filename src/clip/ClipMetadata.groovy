package clip

// Sends information about the clips to the frontend
class ClipMetadata {

  public static Map getMetadataForClip(String clipId) {
    ClipMetadata.getClipMetadata().find { data ->
      data.clipId == clipId
    }
  }

  // Only 'solid_color' is hooked up right now, so the controls for the other clips are dummy data that will be
  // replaced later
  public static List<Map> getClipMetadata() {
    [
        [
            displayName: 'Solid Color',
            clipId     : 'solid_color',
            controls   : [
//                [displayName: 'Hue', type: 'knob', defaultValue: 1.0, fieldName: 'p1'],
//                [displayName: 'Saturation', type: 'knob', defaultValue: 1.0, fieldName: 'p2'],
//                [displayName: 'Brightness', type: 'knob', defaultValue: 1.0, fieldName: 'p3'],
                [displayName: 'Red', type: 'slider', defaultValue: 1.0, fieldName: 'p4'],
                [displayName: 'Green', type: 'slider', defaultValue: 1.0, fieldName: 'p5'],
                [displayName: 'Blue', type: 'slider', defaultValue: 1.0, fieldName: 'p6'],
            ],
        ],
        [
            displayName: 'Color Wash',
            clipId     : 'color_wash',
            controls   : [
                [displayName: 'Angle', type: 'knob', defaultValue: 0.5, fieldName: 'p1'],
                [displayName: 'Speed', type: 'knob', defaultValue: 0.5, fieldName: 'p2'],
                [displayName: 'Spread', type: 'knob', defaultValue: 0.5, fieldName: 'p3'],
//                [displayName: 'Red Cut', type: 'knob', defaultValue: 1.0, fieldName: 'p4'],
//                [displayName: 'Green Cut', type: 'knob', defaultValue: 1.0, fieldName: 'p5'],
//                [displayName: 'Blue Cut', type: 'knob', defaultValue: 1.0, fieldName: 'p6'],
            ],
        ],
        [
            displayName: 'Node Scan',
            clipId     : 'node_scan',
            controls   : [
                [displayName: 'Speed', type: 'knob', defaultValue: 1.0, fieldName: 'p1'],
                [displayName: 'Length', type: 'knob', defaultValue: 1.0, fieldName: 'p2'],
                [displayName: 'Brightness', type: 'knob', defaultValue: 1.0, fieldName: 'p3'],
//                [displayName: 'Red', type: 'knob', defaultValue: 1.0, fieldName: 'p4'],
//                [displayName: 'Green', type: 'knob', defaultValue: 1.0, fieldName: 'p5'],
//                [displayName: 'Blue', type: 'knob', defaultValue: 1.0, fieldName: 'p6'],
            ],
        ],
        [
                displayName: 'Particle Clip',
                clipId     : 'particle_clip',
                controls   : [
                        [displayName: 'Size',         type: 'slider', defaultValue: 0.5, fieldName: 'p1'],
                        [displayName: 'Ramp',         type: 'slider', defaultValue: 0.5, fieldName: 'p2'],
                        [displayName: 'Speed',        type: 'slider', defaultValue: 0.5, fieldName: 'p3'],
                        [displayName: 'Acceleration', type: 'slider', defaultValue: 0.0, fieldName: 'p4'],
                        [displayName: 'Density',      type: 'slider', defaultValue: 0.5, fieldName: 'p5'],
                        [displayName: 'Blue', type: 'knob', defaultValue: 1.0, fieldName: 'p6'],
                        [displayName: 'Blue', type: 'knob', defaultValue: 1.0, fieldName: 'p7'],
                        [displayName: 'Blue', type: 'knob', defaultValue: 1.0, fieldName: 'p8']

                ],
        ],
        [
                displayName: 'Perlin Noise',
                clipId     : 'perlin_noise',
                controls   : [
                        [displayName: 'Speed',      type: 'slider',   defaultValue: 0.5, fieldName: 'p1'],
                        [displayName: 'Scale',      type: 'slider', defaultValue: 0.5, fieldName: 'p2'],
                        [displayName: 'Threshold',  type: 'slider', defaultValue: 0.5, fieldName: 'p3'],
                        [displayName: 'Lod',        type: 'slider', defaultValue: 0.5, fieldName: 'p4'],
                        [displayName: 'Falloff',    type: 'slider', defaultValue: 0.5, fieldName: 'p5'],
                        [displayName: 'X',          type: 'knob', defaultValue: 0.0, fieldName: 'p6'],
                        [displayName: 'Y',          type: 'knob', defaultValue: 0.0, fieldName: 'p7'],
                        [displayName: 'Z',          type: 'knob', defaultValue: 0.0, fieldName: 'p8']

                ],
        ],
        [
            displayName: 'Video',
            clipId     : 'video',
            controls   : [
                [displayName: 'Video File', type: 'videoFile', defaultValue: '24K_loop-nosound.mp4', fieldName: 'filename'],
            ],
        ],
    ]
  }

}
