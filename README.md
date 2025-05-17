# Jet Tts

Jet Tts is a lightweight [Text to Speech](https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html) implementation with additional features and basic UI in Jetpack Compose.

**IMPORTANT:** Keep in mind that `utteranceId` should be unique for each utterance across whole app, othwerwise you are at risk of inconsistent text highlighting.

![Ilustration image](/images/showcase.gif)

### Add queries block to `AndoridManifest.xml`

> Apps targeting Android 11 that use text-to-speech should declare
`TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE` in the queries elements of their manifest:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

### Create TtsClient instance
Since `TtsClient` is using `Context` it's recommended to use single `TtsClient` instance in your application, you can use [CompositionLocalProvider](https://developer.android.com/develop/ui/compose/compositionlocal).

```kotlin
//Define CompositionLocalProvider that will provide client
val LocalTtsClient: ProvidableCompositionLocal<TtsClient> = compositionLocalOf(
    defaultFactory = { error("Client not available") }
)

...

@Composable
fun MainScreen() {
//Create TtsClient
val ttsClient = rememberTtsClient()
CompositionLocalProvider(
    LocalTtsClient provides ttsClient
) {
    //Application content
  }
}
```

## Features

### Text highlight Feature (api >= 26)
Using `TtsClient.HighlightMode` to set how you want to highlight currently spoken text:
* `SPOKEN_WORD` - `TextTts` will highlight currently spoken sequence (single word in most cases).
* `SPOKEN_RANGE_FROM_BEGINNING` - `TextTts` will highlight range from the beggining to the currently spoken sequence.

```kotlin
//Setting highlightMode when creating client
val ttsClient = rememberTtsClient(
    highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING
)

...

//Setting highlightMode using client's parameter
val ttsClient.highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING
```


### Autoscroll Feature (api >= 26)
By providing a `ScrollState`, `TextTts` can use it to autoscroll to currently spoken line. Solution for `LazyColumn` is not avaliable now.

```kotlin
val scrollState = rememberScrollState()
Column(
    modifier = Modifier
        .verticalScroll(state = scrollState)
) {
    TextTts(
        text = "",
        ttsClient = ttsClient,
        utteranceId = "content",
        scrollState = scrollState,
    )
}
```

### Navigation Feature
It is possible to "navigate" in utterance when `ttsClient.isSpeaking == true`, by clicking into `TextTts` client will navigate speech by clicked word.


## Examples


### Simple Usage Example

```kotlin
//Text to be shown and spoken by tts
const val text = "Hello World"
//Id for utterance request, this is required for getting callbacks from UtteranceProgressListener.UtteranceProgressListener
const val utteranceId = "greeting"

//For getting client Instance
val ttsClient = rememberTtsClient()

Column() {
    TextTts(
        text = text,
        utteranceId = utteranceId,
        ttsClient = ttsClient,
    )

    Button(
        onClick = {
            ttsClient.speak(text = text,utteranceId = utteranceId)
        },
    ) {
        Text(text = "Speak")
    }
}
```

### Usage with handling TtsClient state and customization (limited)

```kotlin
//Text to be shown and spoken by tts
const val text = "Hello World"
//Id for utterance request
const val utteranceId = "greeting"

//For getting client Instance
val ttsClient = rememberTtsClient(
    highlightMode = TtsClient.HighlightMode.SPOKEN_RANGE_FROM_BEGINNING, //Setting highligt mode
    onInitialized = { ttsClient ->
        ttsClient.setLanguage(language = Locale.US) //Setting language by locale (depends if language is supported)
    },
)

Column() {
    TextTts(
        text = text,
        utteranceId = utteranceId,
        ttsClient = ttsClient,
        highlightStyle = TextStyle(color = Color.Red) //Setting custom highlight style
    )

    Button(
        onClick = {
            //Using ttsClient.isSpeaking state to handle play/stop
            if (!ttsClient.isSpeaking) {
                ttsClient.speak(
                    text = text,
                    utteranceId = utteranceId,
                    queueMode = TextToSpeech.QUEUE_FLUSH, //Use QUEUE_FLUSH for replacing queue of QUEUE_ADD for add utterance to queue
                )
            } else {
                ttsClient.stop()
            }
        },
    ) {
        //Using ttsClient.isSpeaking state to show proper text on button
        Text(
            text = if (!ttsClient.isSpeaking) "Speak" else "Stop"
        )
    }
}
```

### Tips
* Always use `flushAndSpeak` when you want to use tts on different screen than before, make sure the old queue is flushed properly.
* Use tts with Activities or [Compose Navigation](https://developer.android.com/develop/ui/compose/navigation) to ensure proper internal functionality.

### Warnings (What not to do)
* Do not use different string in UI and in the client
* Do not change text under `utteranceId`