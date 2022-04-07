ping -W 300 -t 20 HOSTNAME | grep --line-buffered -oP '(?<=time=)[0-9]*'
