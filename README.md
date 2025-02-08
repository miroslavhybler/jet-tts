# Jet Tts
Jet Tts is a lightweighted [Text to Speech](https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html) implementation with text highlight feature and basic UI in Jetpack Compose.


### Add queries block to `AndoridManifest.xml`
> Apps targeting Android 11 that use text-to-speech should declare `TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE` in the queries elements of their manifest:
```xml
<queries>
     <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

### Simple Usage Example
```kotlin
//Text to be shown and spoken by tts
const val text = "Hello World"
//Id for utterance request
const val utteranceId = "greeting"

//For getting client Instance
val ttsClient = rememberTtsClient()

Column() {
    TextTts(
        text= text,
        utteranceId = utteranceId,
        ttsClient = ttsClient,
    )

    Button(
        onClick = {
            ttsClient.speak(
                text=text, 
                utteranceId=utteranceId,
            )
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
        text= text,
        utteranceId = utteranceId,
        ttsClient = ttsClient,
        highlightStyle = TextStyle(color = Color.Red) //Setting custom highlight style
    )

    Button(
        onClick = {
            //Using ttsClient.isSpeaking state to handle play/stop
            if (!ttsClient.isSpeaking) {
                ttsClient.speak(
                    text=text,
                    utteranceId=utteranceId,
                    queueMode = TextToSpeech.QUEUE_FLUSH, //Use QUEUE_FLUSH for replacing queue of QUEUE_ADD for add utterance to queue
                )
            } else {
                ttsClient.stop()
            }
        },
    ) {
        //Using ttsClient.isSpeaking state to show proper text on button
        Text(
            text = if (!ttsClient.isSpeaking)  "Speak" else "Stop"
        )
    }
}
```