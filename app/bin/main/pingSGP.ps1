ping -w 300 -t HOSTNAME | Select-String -Pattern "time=.*ms" | foreach {$_.Matches.Value.substring(5).TrimEnd('ms')}