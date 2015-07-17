Pomodoro for Android Wear
=========================

[![Build Status](https://travis-ci.org/vngrs/PomoPomoAndroid.png?branch=develop)](https://travis-ci.org/vngrs/PomoPomoAndroid)
[![Join the chat at https://gitter.im/vngrs/PomoPomoAndroid](https://img.shields.io/badge/GITTER-join%20chat-green.svg)](https://gitter.im/vngrs/PomoPomoAndroid?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)

Installation
------------

Download the application with the following. [link](https://play.google.com/store/apps/details?id=com.vngrs.android.pomodoro) after you become a tester. 

What
----
Yet Another Android Pomodoro Client

Why
---
There are already bunch of Pomodoro Applications on the Play Store. And there are couple of Android Wear spesific apps. Most of them are even open-source. 

I personally had problems with them and wanted to write one from scratch.

What does this repo have extra?
- Demonstrates a good example of separate Android Wear app with custom embedded `Activity` in the notification.
- Code sharing between the phone part and wear part. 
- Product flavors with Android Wear. 
- Usage of Dagger 2. 
  - It is great for both the shared code between mobile and wear parts and additionally for product flavor differences. 
- [FUTURE] Colloborative Pomodoro with Google Cast support. How cool is that. :)

Things TODO
-----------
You can contribute and see the TODO list with the following Trello board.
https://trello.com/b/lU9UHl2B/pomodoro-android

* [X] Phone app support.
* [X] Phone app notification sync with Wear app.
* [ ] Pomodoro WatchFace.
* [ ] Step detection.
* [ ] Logging what to do and what's done with voice.

License
--------

    © 2015 VNGRS

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/vngrs/pomopomoandroid/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
