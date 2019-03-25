# AudiobookPlayer
A library to play titles from a public domain audiobook collection.

AudiobookPlayer is a library that allows a client to play from a collection of public domain audiobooks. It supports the following actions:
- playing
- stopping
- pausing
- resuming
- seeking
- progress reporting

Binding to this library returns an instance of AudiobookService.MediaControlBinder, which provides an interface to all supported features.
Progress reporting is supported via a handler.

Your application must declare the android.permission.INTERNET permission to use this library.
