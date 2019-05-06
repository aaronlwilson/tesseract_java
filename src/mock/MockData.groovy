package mock

import groovy.json.JsonBuilder

// Returns JSON
class MockData {

  public static List<Map> getSceneStoreData() {
    [
        [
            id          : 1,
            displayName : 'Show Some Color',
            channel1Clip: [clipId: 'solid_color', controlSetId: 1],
            channel2Clip: [clipId: 'color_wash', controlSetId: 2],
        ],
        [
            id          : 2,
            displayName : 'Text Scroll',
            channel1Clip: [clipId: 'solid_color', controlSetId: 1],
            channel2Clip: [clipId: 'text_scroll', controlSetId: 3],
        ],
        [
            id          : 3,
            displayName : 'Colored Image Scroll',
            channel1Clip: [clipId: 'image_scroll', controlSetId: 4],
            channel2Clip: [clipId: 'color_wash', controlSetId: 2],
        ],
        [
            id          : 4,
            displayName : 'Color Gif',
            channel1Clip: [clipId: 'solid_color', controlSetId: 1],
            channel2Clip: [clipId: 'animated_gif', controlSetId: 5],
        ],
    ]
  }

  public static List<Map> getPlaylistStoreData() {
    [
        [
            id         : 1,
            displayName: 'Rad Playlist',
            items      : [
                [sceneId: 1, duration: 234],
                [sceneId: 2, duration: 53],
                [sceneId: 1, duration: 123],
                [sceneId: 3, duration: 533],
                [sceneId: 1, duration: 132],
                [sceneId: 4, duration: 54],
            ],
        ],
        [
            id         : 2,
            displayName: 'Image Playlist',
            items      : [
                [sceneId: 3, duration: 56],
                [sceneId: 4, duration: 234],
                [sceneId: 3, duration: 111],
                [sceneId: 4, duration: 564],
                [sceneId: 3, duration: 755],
                [sceneId: 4, duration: 554],
            ],
        ],
    ]
  }

}
