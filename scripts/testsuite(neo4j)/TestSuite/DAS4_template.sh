#!/bin.bash

#zalogowac sie do DAS4
sshpass -p myrootpass ssh -l root localhost //man page filepassing security issues
http://www.softwareprojects.com/resources/programming/t-ssh-no-password-without-any-private-keys-its-magi-1880.html

#- odpalic cluster
#- czekac ls  az sie odpali
#- copy all input
rsync --rsh='sshpass -p 12345 ssh -l test' host.example.com:path .
http://www.crucialp.com/resources/tutorials/server-administration/how-to-copy-files-across-a-network-internet-in-unix-linux-redhat-debian-freebsd-scp-tar-rsync-secure-network-copy.php

#- [execute job]
#- copyLocal output
#- shutdown cluster
#- fetch logs+output
rsync