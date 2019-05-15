package clip

// Sends information about the clips to the frontend
class ClipMetadata {

  // Only 'solid_color' is hooked up right now, so the controls for the other clips are dummy data that will be
  // replaced later
  public static List<Map> getClipMetadata() {
    [
        [
            displayName: 'Solid Color',
            clipId     : 'solid_color',
            controls   : [
                [displayName: 'Hue', type: 'knob', defaultValue:  1.0, fieldName: 'p1'],
                [displayName: 'Saturation', type: 'knob', defaultValue:  1.0, fieldName: 'p2'],
                [displayName: 'Brightness', type: 'knob', defaultValue:  1.0, fieldName: 'p3'],
                [displayName: 'Red', type: 'slider', defaultValue:  1.0, fieldName: 'p4'],
                [displayName: 'Green', type: 'slider', defaultValue:  1.0, fieldName: 'p5'],
                [displayName: 'Blue', type: 'slider', defaultValue:  1.0, fieldName: 'p6'],
            ],
        ],
        [
            displayName: 'Color Wash',
            clipId     : 'color_wash',
            controls   : [
                [displayName: 'Angle', type: 'knob', defaultValue:  1.0, fieldName: 'p1'],
                [displayName: 'Speed', type: 'knob', defaultValue:  1.0, fieldName: 'p2'],
                [displayName: 'Spread', type: 'knob', defaultValue:  1.0, fieldName: 'p3'],
                [displayName: 'Red Cut', type: 'knob', defaultValue:  1.0, fieldName: 'p4'],
                [displayName: 'Green Cut', type: 'knob', defaultValue:  1.0, fieldName: 'p5'],
                [displayName: 'Blue Cut', type: 'knob', defaultValue:  1.0, fieldName: 'p6'],
            ],
        ],
        [
            displayName: 'Node Scan',
            clipId     : 'node_scan',
            controls   : [
                [displayName: 'Hue', type: 'knob', defaultValue:  1.0, fieldName: 'p1'],
                [displayName: 'Saturation', type: 'knob', defaultValue:  1.0, fieldName: 'p2'],
                [displayName: 'Brightness', type: 'knob', defaultValue:  1.0, fieldName: 'p3'],
                [displayName: 'Red', type: 'knob', defaultValue:  1.0, fieldName: 'p4'],
                [displayName: 'Green', type: 'knob', defaultValue:  1.0, fieldName: 'p5'],
                [displayName: 'Blue', type: 'knob', defaultValue:  1.0, fieldName: 'p6'],
            ],
        ],
        [
            displayName: 'Stripe',
            clipId     : 'stripe',
            controls   : [
                [displayName: 'Hue', type: 'knob', defaultValue:  1.0, fieldName: 'p1'],
                [displayName: 'Saturation', type: 'knob', defaultValue:  1.0, fieldName: 'p2'],
                [displayName: 'Brightness', type: 'knob', defaultValue:  1.0, fieldName: 'p3'],
                [displayName: 'Red', type: 'knob', defaultValue:  1.0, fieldName: 'p4'],
                [displayName: 'Green', type: 'knob', defaultValue:  1.0, fieldName: 'p5'],
                [displayName: 'Blue', type: 'knob', defaultValue:  1.0, fieldName: 'p6'],
            ],
        ],
    ]
  }

}
