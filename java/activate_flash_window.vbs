Dim objShell
set objShell = createobject("wscript.shell")
objShell.AppActivate("Adobe Flash Player 10")
objShell.SendKeys "% {Enter}"
