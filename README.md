# Data analyzer

![alt text][app_logo]\
The ic_launcher.png image is an original image created on the [Canva][canva] platform under license [CC-BY-4.0][cc-by-4.0]

## Overview

An application in which you can conduct research on: transfer time,
transfer speed, connection quality (BT module) by sending files while connected to another
device via the same app. Bluetooth and Wi-Fi Direct technologies are used.\
The given application was used for research included in my master's thesis entitled "Analysis of data 
transmission in Bluetooth and Wi-Fi technologies in communication between mobile devices".

### Features

**Common to the entire application:**
- Displaying information and error logs
- Enable Location if it is disabled
- Changing the language to: Polish, English

**Common for BT and Wi-Fi module:**
- Connecting the Server(Host)/Client type with the device listed as Client
- Selecting a file to send (Client)
- Selection of buffer size during transfer (Client)
- Specifying how many times the file should be sent (Client)
- Display of file and upload information (Client)
- Display of upload progress at Host and Client
- Displaying file data with the name that was saved (Host)
- Graphical display of measurement data (Client)
- Saving data to a file (Client)
- Possibility of disconnecting from both sides

**Common for BT module:**
- Enable BT if it is not enabled
- Enable BT discoverability
- Find discoverable BT devices (for 12s)
- Pairing with a found device
- Connection quality display (RSSI value + 100)
- After disconnecting, the ability to reconnect in the same client-host configuration

**Common for Wi-Fi module:**
- Enable Wi-Fi if it is not enabled
- Discoverability of the device by the Wi-Fi Direct module
- Find discoverable Wi-Fi Direct devices

## Install

To install the software, download the APK installation package: [Installer][installer]
 and then run it on the target device for installation.

## Used technologies/libraries

Other icons used in the project are used under the [Apache License, Version 2.0][apache_license]

The following were used to display the charts:
- [MPAndroidChart v3.1.0][mpandroidchart] library by [PhilJay][philjay] licensed under
[Apache License, Version 2.0][apache_license]

## Screenshots

The screenshots shown below are licensed [CC-BY-4.0][cc-by-4.0]

![alt text][main_menu]
![alt text][languages]
![alt text][bt_module]
![alt text][wifi_module]
![alt text][after_send_file]
![alt text][graph]

## License
[License MIT][license] Copyright © 2023 Bartosz Gieras

[app_logo]: https://github.com/Giero98/MasterThesis/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
[main_menu]:  https://github.com/Giero98/MasterThesis/blob/main/app_screens/main_menu.png
[languages]: https://github.com/Giero98/MasterThesis/blob/main/app_screens/languages.png
[bt_module]: https://github.com/Giero98/MasterThesis/blob/main/app_screens/bt_module.png
[wifi_module]: https://github.com/Giero98/MasterThesis/blob/main/app_screens/wifi_module.png
[after_send_file]: https://github.com/Giero98/MasterThesis/blob/main/app_screens/after_send_file.png
[graph]: https://github.com/Giero98/MasterThesis/blob/main/app_screens/graph.png
[installer]: https://github.com/Giero98/DataAnalyzer/raw/v1.0.0/app/release/DataAnalyzer-installer-v1.0.0.apk
[canva]: https://www.canva.com/
[cc-by-4.0]: https://creativecommons.org/licenses/by/4.0/deed.en
[mpandroidchart]: https://github.com/PhilJay/MPAndroidChart
[philjay]: https://github.com/PhilJay
[apache_license]: https://www.apache.org/licenses/LICENSE-2.0
[license]: https://github.com/Giero98/MasterThesis/blob/main/LICENSE
