SUN=/opt/sun-jdk-1.5.0.12/
CC=gcc

client:
	$(CC) -fPIC -O3 -I$(SUN)/include -I$(SUN)/include/linux -I./build/headers -shared -o libWebcam.so src/native/linuxwebcam.c

#server:
#	$(CC) -fPIC -O3 -I$(SUN)/include -I$(SUN)/include/linux -I./build/headers -I$(CXROOT) -shared -o libPXHI.so src/native/PXHI.c

