TimeSpent
=========

- Keep track of your time as you work on multiple projects using a global mouse motion listener.
- An `Idle Threshold` allows you to remain active while not actively using the computer (such as working on a white board, writing in a notebook, reading, etc.).  Time without mouse motion won't be counted as idle until this value is exceeded.
- Progress is saved periodically to an easily editable XML file while you work and later restored when the application is launched again.
- Each timer can be paused, resumed, adjusted, and reset through the UI (with a right click) to accurately reflect reality.  For example, if you go to an unexpected meeting without first pausing the `Since Last Movement` timer, all of the time spent in the meeting will be counted as idle once the threshold value is exceeded.  To fix this, you would right click on the timer for the appropriate project, select `Adjust`, and then enter the number of seconds needed to correct the time.  e.g. 10 minutes would be `600` seconds, subtracting 10 minutes would be `-600` seconds.

![](http://i.imgur.com/J6bp7GV.png)

Build
---

`gradle shadowJar`



