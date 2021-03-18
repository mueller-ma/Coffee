# Coffee

This Android app allows you to keep the display awake without having to change the device settings. It can be toggled at various places:
* A tile in the quick settings, the place that holds the toggles for e.g. Wi-Fi and Bluetooth. Requires Android 7+.
* A button in the app itself
* A shortcut on your home screen. It can be created in the app, if your launcher supports it.

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/de/packages/com.github.muellerma.coffee/)

## Translations

You can help translating this app by coping the file `app/src/main/res/values/strings.xml` to `app/src/main/res/values-xx/strings.xml` (`xx` stands for your language code), removing all entries marked with `translatable="false"` and translating the remaining entries. This file contains all strings used in the app.

The app store description can also be translated: Copy `fastlane/metadata/android/en-US/full_description.txt` and `fastlane/metadata/android/en-US/short_description.txt` to `fastlane/metadata/android/xx-YY/full_description.txt` (`xx` is the language code, `YY` is the country code) and translate the file.

## Troubleshooting

Please consult the help dialog of the app if you have any issues with Coffee. It can help you with disabling battery saver.
If the app still doesn't keep the display awake, it might be caused by these battery savers. There're some devices known not to work: https://github.com/mueller-ma/Coffee/issues?q=label%3A%22battery+saver%22

Feel free to open an issue if your device isn't in the list of closed issues.

## Credits

* Feature graphic by https://unsplash.com/@mukulwadhwa
