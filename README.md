TimeSpent
=========

- Keep track of your time as you work on multiple projects using a global mouse motion listener.
- Idle threshold allows you to remain active while not actively using the computer (working on a white board, writing in a notebook, reading, etc.).  Time without mouse motion won't be counted as idle until this value is exceeded.
- Progress is saved periodically to an easily editable XML file while you work and restored when the application is launched.
- Timers can be paused, resumed, adjusted, and reset through the UI (with a right click) to accurately reflect what happened.  For example, if you go to an unexpected meeting without first pausing the `Since Last Movement` timer, all of that time spent in the meeting will be counted as idle once the threshold value is passed.  To fix this, you would adjust time for this project and add whatever amount was accumulated as idle incorrectly.

![](http://i.imgur.com/J6bp7GV.png)

Build
---

`gradle shadowJar`



