# hrs-cordova-plugin-audiomanagement
A plugin to manage audio mode and volume

## Usage

```typescript
import {AudioManagement, VolumeType} from 'hrs-cordova-plugin-audiomanagement';

async function changeVolume() {
    const volumePercentage = 75;
    await AudioManagement.setVolume(VolumeType.MUSIC, volumePercentage);
}
```

## Contributing

### Prerequisites

1. make sure you have [nvm](https://github.com/nvm-sh/nvm) installed
2. run `nvm use` at the root of the project to use the correct version (install if needed)
3. run `npm install`

### Making changes

To add a new API or modify existing ones:

1. make any necessary changes in the typescript API at `src/ts/cordova-plugin-audiomanagement.ts`
2. make any necessary changes in android layer at `src/android/AudioManagement.java`
3. sync the typescript API by running `npm run build`
4. update the plugin version after changes are made by running the [npm version command](https://docs.npmjs.com/cli/v8/commands/npm-version)
