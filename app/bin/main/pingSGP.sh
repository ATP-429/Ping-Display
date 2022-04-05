ping -w 300 -t 20 pingtest-sgp.brawlhalla.com | grep --line-buffered -oP '(?<=time=)[0-9]*'
