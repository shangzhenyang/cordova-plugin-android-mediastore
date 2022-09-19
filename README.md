# cordova-plugin-android-mediastore
This simple Cordova plugin converts a base64 bytestring to an image file and adds it to the gallery. It supports `image/png`, `image/jpeg`, and `image/webp`.

This fork adds the support for `image/jpeg` and `image/webp`. It will infer the MIME type based on the file extension.

Tested on Android APIs 22 (5.1 Lollipop), 29 (10 Q) and 30 (11 R).

Useful when capturing an HTML canvas.

## API
The plugin exports only one function:

```javascript
//CordovaAndroidMediaStore.js

/**
 * @param byteString the bytestring part of the base64 dataURI (excludes the MIME part, see the example)
 * @param fileDir the relative directory to save the file to.
 * @param fileName
 */
exports.store = function (byteString, fileDir, fileName, success, error) {
    exec(success, error,
        'CordovaAndroidMediaStore',
        'store',
        [byteString, fileDir, fileName]);
};
```

## Example
```typescript
let dataURItoGallery: (dataURI: string) => Blob = (dataURI) => {
    // Get bytestring part of the image dataURI
    let byteString = dataURI.split(",")[1];
    cordova.plugins.CordovaAndroidMediaStore.store(byteString, "Pictures", `${Date.now()}.png`);
};
```
