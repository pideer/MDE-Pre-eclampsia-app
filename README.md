hello every one!
i should be moving this repo to another github account and deleting toluk-26. please look out my personal account [pideer](https://github.com/pideer)

# TODO
 - [ ] critical threshold implementation (firmware as well) 
 - [ ] go to connect screen on device disconnect
 - [ ] confirm calibrate
 - [ ] implement the [nordic dfu library](https://github.com/NordicSemiconductor/Android-DFU-Library) for ota updates. `implementation 'no.nordicsemi.android:dfu:2.11.0'`
 - [ ] R.string all static string content and then language translations
 - [ ] play store account for publishing or sideloading deployment.
 - [ ] check if fonts is right
 - [ ] developer and package name. rename the `com.example.pre_eclampsia` ts


# Future team notes
- app icon is [icon.svg](/icon.svg)
- entrypoint is main MainActivity.kt / PESApp.kt
- ble.* has the all bluetooth functions. MainApplication makes it global. idk too much how it works
  - data.* has the data class that repo.* singletons will hold.
  - managers.* maintains the interactions for each service. parsers.* are called by manangers to parse the incoming data packet
  - BleManager maintained the scan and connection, but it is moved to the ScanViewModel.
- Calibrate hasn't been fully tested oopsies
- sorry for the lack of comments 😭. email my academic email for questions.
- please increment the version in [build.gradle.kts (Module :app)](/app/build.gradle.kts)!!