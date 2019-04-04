# AudiobookPlayer
A library to play titles from a public domain audiobook collection.

The library can be downloaded [here](https://kamorris.com/lab/audlib/audiobook-player.aar)

AudiobookPlayer is a library that allows a client to play from a collection of public domain audiobooks hosted on a web service. It supports the following actions:
- playing
- stopping
- pausing
- resuming
- seeking
- progress reporting

Binding to the service edu.temple.audiobookplayer.AudiobookService, found in this library, returns an instance of AudiobookService.MediaControlBinder, which provides an interface for all supported features.
Progress reporting is supported via a handler.

AudiobookService returns an instance of MediaControlBinder when bound to. MediaControlBinder contains the following functions:
- **play(int id)**: Downloads and begins playing the audiobook for the provided ID
- **play(int id, int position)**: Downloads and begins playing the audiobook for the provided ID at the specified position in seconds
- **play(File file)**: Begins playing the specified file
- **play(File file, int id)**: Begins playing the specified file from the specified position in seconds
- **pause()**: Pauses the currently playing audiobook, or plays the audiobook if paused
- **stop()**: Stops the curently playing audiobook
- **setProgressHandler(Handler progressHandler)**: Accepts a handler that will be used to provide progress updates. Message.what will contain an int representing the progress of the book thus far in seconds
- **seekTo(int position)**: Jumps to specified position in audiobook. Does not interrupt playback
