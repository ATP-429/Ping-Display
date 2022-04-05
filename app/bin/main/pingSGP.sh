#!/bin/sh
ping -w 300 -t 20 pingtest-sgp.brawlhalla.com | grep -oP '(?<=time=)[0-9]*'
