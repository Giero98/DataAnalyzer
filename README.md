# MasterThesis

![alt text][app_logo]\
Original image created on the [Canva][canva] platform under license [CC-BY-4.0][

## Overview
An application in which a device using Bluetooth and Wi-Fi transfers files to another and measures the transfer time.

### Features

**Common to the entire application:**
- displaying information and error logs

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

## Used technologies/libraries

## Screenshots

## How to install

## License
[License][license] © Bartosz Gieras

[app_logo]: https://github.com/Giero98/MasterThesis/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
[canva]: https://www.canva.com/
[cc-by-4.0]: 
[license]: https://github.com/Giero98/MasterThesis/blob/main/LICENSE
