## FileMan

FileMan is a simple portable file manager/browser written in pure java/swing. The project is a reworked version of file browser found on stackexchange. The difference/update is that we don't have to mark folder in file tree to know if it has any children (if it contains any files/folders). Some features have been removed to make code simpler.

There are actually two versions:
- FileManagerEager
- FileManagerLazy

There is substantial difference in how each of them work - the lazy one at first shows only the top directory and only through subsequent expand operations we can see what lies deeper. That is not the case with eager file manager which loads whole hierarchy at once allowing to expand all nodes down to each leaf at start. This of course comes at a cost because such operation may take a while - it is advised to use this manager only with known reasonable amount of files to process. The user also has to be wary about soflinks as they can be infinitely nested and may cause stack overflow.

- https://codereview.stackexchange.com/questions/4446/file-browser-gui
- https://stackoverflow.com/questions/58018068/jtable-cell-renderer-set-backround-using-other-cells-background
