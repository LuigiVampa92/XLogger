# XLogger
Xposed module that captures and records messages transmitted via NFC and Bluetooth interfaces on the device. Requires Xposed framework to be installed on the device.

## What does it do?

This module allows you to sniff and record messages transmitted via NFC and Bluetooth.

Note that it can intercept only the data that is sent or received by YOUR device and not just ANY wireless devices around you that communicate with each other over the air.

The main purpose of this module is to help to understand what and how exactly applications transmit with device's wireless interfaces, to assist in reverse engineering of Android apps that use Bluetooth of NFC API, and perhaps to fulfill the curiosity of some people who would like to see what that app does under the hood but they don't want to invest in special hardware and setup the wireless lab.

## What can I use it for?

Currently app supports NFC and Bluetooth communication capture. Here are some possible examples:
- Most common examples for NFC usage on Android would be companion mobile applications of ticket systems or loyalty cards that read or emulate NFC tags. It can also be payment systems applications, but note that payment apps tend to implement complex root, environment and instrumentation checks, as well as SafetyNet check. So you will have to bypass those checks by yourself in order to capture their communications. This Xposed module will not directly assist you in that.
- Bluetooth is often used in companion mobile applications for IoT devices, all kinds of smart home devices, peripheral devices etc.

Support of other Android APIs may be added to this module in future.

## Installation

- Install this app on a device that has Xposed Framework installed (current version is [LSPosed](https://github.com/LSPosed/LSPosed)).
- Activate the Xposed module and enable it for applications which NFC or Bluetooth communications you would like to capture and inspect.

## Usage

After NFC or Bluetooth communication took place, open the XLogger app, and you will see a new record with captured data. You can see its content and export it as a file.

Note that currently it takes some time to save Bluetooth communication logs. There is no reliable way to define that Bluetooth communication session stopped that works on all device models, that's why timeout from last transmitted message is used to consider that communication is over and captured logs should now be saved. It may take up to 15-20 seconds in some cases. So DO NOT close the app that just used Bluetooth, wait for some time or just minimize it to make sure its process will not stop before captured data will be saved.

NFC communication logs are captured and saved almost immediately.

## Limitations

This is a very first published version, so something might not be implemented or work well. Something might be fixed in future, something might not be fixed at all:

- What you see in the captured data log is not a 100% complete log of every byte send and received by device, but rather the "application level" part that is available inside Android framework. For example, some checksum bytes, some packet headers are not logged because Android API just deal with these things under the hood and do not expose them to Android framework. It will stay that way, but that's completely ok, these bytes carry no valuable information and even when you capture wireless data with special hardware they are usually filtered and thrown off anyway.
- Currently this module does not capture NFC tags with NDEF-formatted messages that are implicitly read by internal system API and trigger a broadcast of intent with "android.nfc.action.NDEF_DISCOVERED" action. This is because the raw bytes never show up in normal part of android framework. Not sure if it is possible to solve this problem.
- Some system features like Android Nearby API are not captured for the same reasons as above. Maybe I will find a way to deal with it, but later.
- Currently no data captured if device communicates with Mifare Classic NFC tags. This is because Mifare Classic stands very far from normal standards that are used by all other manufacturers that make wireless cards, payment cards and any ICC or UICC in general. Even Android itself uses a separate API to work with this type of tags and not the normal one. Also, while all other types of NFC tags are guaranteed to be supported on any Android device with NFC chip, Mifare Classic tags are hardware-dependant. They can be read only if specific NFC chips from a specific manufacturer are used in the device and thus they will NOT work on every Android device, only at some share of the devices. I will probably add its support later, but it will still be more of a "text logs" rather than raw bytes like with other tags.
- Currently only Bluetooth Low Energy GATT communications are captured, no other modes and no classic old Bluetooth RFCOMM sockets. Standard Bluetooth is a bit harder to hook and tbh rarely used in modern peripheral devices, because Apple does not provide API to use it on iOS and most manufacturers of bluetooth devices do not wish to be locked only on clients with Android smartphones. The support of this feature may be added in future versions.   
