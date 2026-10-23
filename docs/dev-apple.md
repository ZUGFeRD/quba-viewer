# Developer notes about Apple

## Commands about signing and notarization

### Verify signature

```bash
BINARY="Quba.app"
codesign -v -v "${BINARY}"
```

### Show signature details

```bash
BINARY="Quba.app"
codesign --display -vvv "${BINARY}"
```

### Start notarization

```bash
APP="Quba.dmg"
APPLE_ID=""
TEAM_ID=""
PASSWORD=""
NOTARIZATION_ID=""

xcrun notarytool submit \
  --apple-id "${APPLE_ID}" \
  --team-id "${TEAM_ID}" \
  --password "${PASSWORD}" \
  --wait \
  "${APP}"
```

### Get notarization log

```bash
APPLE_ID=""
TEAM_ID=""
PASSWORD=""
NOTARIZATION_ID=""

xcrun notarytool log \
  --apple-id "${APPLE_ID}" \
  --team-id "${TEAM_ID}" \
  --password "${PASSWORD}" \
  "${NOTARIZATION_ID}" > notarization.json
```

### Verify, if Gatekeeper allows execution 

```bash
APP_BUNDLE="Quba.app"
spctl -a -t exec --ignore-cache -vv "${APP_BUNDLE}"
```

### Staple after successful notarization

```bash
APP="Quba.dmg"
xcrun stapler staple "${APP}"
```

## Additional links

- [Signing and notarizing distributions for macOS](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md)
- [Inside Code Signing: Certificates](https://developer.apple.com/documentation/technotes/tn3161-inside-code-signing-certificates)
- [Resolving common notarization issues](https://developer.apple.com/documentation/security/resolving-common-notarization-issues)
- [Creating Distribution-Signed Code for Mac](https://developer.apple.com/forums/thread/701514)
- [Packaging Mac Software for Distribution](https://developer.apple.com/forums/thread/701581)
- [Signing and Uploading apps to the Mac App Store](https://lessons.livecode.com/a/876834-signing-and-uploading-apps-to-the-mac-app-store)
- [Installing an Apple certificate on macOS runners](https://docs.github.com/en/actions/use-cases-and-examples/deploying/installing-an-apple-certificate-on-macos-runners-for-xcode-development)
