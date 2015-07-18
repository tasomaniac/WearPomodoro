Pomodoro for Android Wear
=========================

[![Build Status](https://travis-ci.org/pomopomo/WearPomodoro.png?branch=develop)](https://travis-ci.org/pomopomo/WearPomodoro)
[![Join the chat at https://gitter.im/pomopomo/WearPomodoro](https://img.shields.io/badge/GITTER-join%20chat-green.svg)](https://gitter.im/pomopomo/WearPomodoro?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)

Installation
------------

https://play.google.com/store/apps/details?id=com.vngrs.android.pomodoro

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
