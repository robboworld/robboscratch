cd firmware\win
avrdude -C avrdude.conf -v -patmega328p -carduino -P %1 -D -Uflash:w:0000.hex:i

