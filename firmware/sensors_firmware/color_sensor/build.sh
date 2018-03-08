#!/bin/bash

clear

avr-gcc -Wall -Os -DF_CPU=8000000UL -mmcu=attiny24 -o main.o main.c
avr-objcopy -j .text -j .data -O ihex main.o main.hex
avrdude -v -p ATtiny24 -cusbasavrdude -v -p ATtiny24 -cusbasp -P usb -Uflash:w:main.hex

