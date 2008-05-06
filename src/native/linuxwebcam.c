#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <linux/videodev.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <fcntl.h>

#include "ibis_mbf_media_LinuxWebcam.h"

#define KcrR 76284
#define KcrG 53281
#define KcbG 25625
#define KcbB 132252
#define Ky 76284

#define RAW   0
#define RGB32 1
#define RGB24 2 

static struct video_picture grab_pic;
static struct video_capability grab_cap;
static struct video_mbuf mbuf;
static struct video_mmap grab_buf;
static unsigned char *grab_data;

static int video_fd;
static int mode;
static int frame_size;
static int next_image = 0; 

// Borrowed from 
// http://search.cpan.org/src/MLEHMANN/Video-Capture-V4l-0.901/RTjpeg/codec/modules/RTcolor_int.c
void yuvrgb32(unsigned char *buf, unsigned char *rgb, int width, int height)
{
	int tmp;
	int i, j;
	int y, crR, crG, cbG, cbB;
	unsigned char *bufcr, *bufcb, *bufy, *bufoute, *bufouto;
	int oskip, yskip;
 
	oskip = width*4;
	yskip = width;
 
	bufcb = &buf[width*height];
	bufcr = &buf[width*height+(width*height)/4];
	bufy = &buf[0];

	bufoute = rgb;
	bufouto = rgb+oskip;
 
	for(i=0; i<(height>>1); i++)
	{
		for(j=0; j<width; j+=2)
		{
			crR = (*bufcr-128)*KcrR;
			crG = (*(bufcr++)-128)*KcrG;
			cbG = (*bufcb-128)*KcbG;
			cbB = (*(bufcb++)-128)*KcbB;
  
			y = (bufy[j]-16)*Ky;
   
			tmp = (y+cbB) >> 16;
			*(bufoute++) = (tmp>255)?255:((tmp<0)?0:tmp);
			tmp = (y-crG-cbG) >> 16;
			*(bufoute++) = (tmp>255)?255:((tmp<0)?0:tmp);
			tmp = (y+crR) >> 16;
			*(bufoute++) = (tmp>255)?255:((tmp<0)?0:tmp);
			
			// set alpha to 255 (opaque)
			*(bufoute++) = 255;

			y=(bufy[j+1]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			*(bufoute++) = 255;

			y=(bufy[j+yskip]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			*(bufouto++) = 255;

			y=(bufy[j+1+yskip]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			*(bufouto++) = 255;
		}
		bufoute+=oskip;
		bufouto+=oskip;
		bufy+=yskip<<1;
	}
}


void yuvrgb24(unsigned char *buf, unsigned char *rgb, int width, int height)
{
	int tmp;
	int i, j; 
	int y, crR, crG, cbG, cbB;
	unsigned char *bufcr, *bufcb, *bufy, *bufoute, *bufouto;
	int oskip, yskip;
 
	oskip=width*3;
	yskip=width;
 
	bufcb=&buf[width*height];
	bufcr=&buf[width*height+(width*height)/4];
	bufy=&buf[0];
	bufoute=rgb;
	bufouto=rgb+oskip;
 
	for(i=0; i<(height>>1); i++)
	{
		for(j=0; j<width; j+=2)
		{
			crR=(*bufcr-128)*KcrR;
			crG=(*(bufcr++)-128)*KcrG;
			cbG=(*bufcb-128)*KcbG;
			cbB=(*(bufcb++)-128)*KcbB;
  
			y=(bufy[j]-16)*Ky;
   
			tmp=(y+cbB)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);

			y=(bufy[j+1]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);

			y=(bufy[j+yskip]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);

			y=(bufy[j+1+yskip]-16)*Ky;

			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
   
		}
		bufoute+=oskip;
		bufouto+=oskip;
		bufy+=yskip<<1;
	}
}

void yuvrgb24_alt(unsigned char *buf, unsigned char *rgb, int width, int height)
{
	int tmp;
	int i, j; 
	int y, crR, crG, cbG, cbB;
	unsigned char *bufcr, *bufcb, *bufy, *bufoute, *bufouto;
	int oskip, yskip;
 
	oskip=width*3;
	yskip=width;
 
	bufcb=&buf[width*height];
	bufcr=&buf[width*height+(width*height)/4];
	bufy=&buf[0];
	bufoute=rgb;
	bufouto=rgb+oskip;
 
	for(i=0; i<(height>>1); i++)
	{
		for(j=0; j<width; j+=2)
		{
			crR=(*bufcr-128)*KcrR;
			crG=(*(bufcr++)-128)*KcrG;
			cbG=(*bufcb-128)*KcbG;
			cbB=(*(bufcb++)-128)*KcbB;
  
			y=(bufy[j]-16)*Ky;

			tmp=(y+crR)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
   			tmp=(y-crG-cbG)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+cbB)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);

			y=(bufy[j+1]-16)*Ky;

			tmp=(y+crR)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+cbB)>>16;
			*(bufoute++)=(tmp>255)?255:((tmp<0)?0:tmp);


			y=(bufy[j+yskip]-16)*Ky;

			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);

			y=(bufy[j+1+yskip]-16)*Ky;

			tmp=(y+crR)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y-crG-cbG)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
			tmp=(y+cbB)>>16;
			*(bufouto++)=(tmp>255)?255:((tmp<0)?0:tmp);
   
		}
		bufoute+=oskip;
		bufouto+=oskip;
		bufy+=yskip<<1;
	}
}


void print_capability() 
{
        printf("name: \"%s\"\n", grab_cap.name);
        printf("type: %d\n", grab_cap.type);
        printf("channels: %d\n", grab_cap.channels);
        printf("audios: %d\n", grab_cap.audios);
        printf("maxwidth: %d\n", grab_cap.maxwidth);
        printf("maxheight: %d\n", grab_cap.maxheight);
        printf("minwidth: %d\n", grab_cap.minwidth);
        printf("minheight: %d\n", grab_cap.minheight);
        printf("\n");
}

void print_picture()
{
        printf("current picture settings:\n");
        printf("brightness: %d\n", grab_pic.brightness);
        printf("hue: %d\n", grab_pic.hue);
        printf("colour: %d\n", grab_pic.colour);
        printf("contrast: %d\n", grab_pic.contrast);
        printf("whiteness: %d\n", grab_pic.whiteness);
        printf("depth: %d\n", grab_pic.depth);
        printf("palette: %d\n", grab_pic.palette);
        printf("\n");
}

void print_buffer()
{
	int i;

        printf("buffer settings:\n");
        printf("total size: %d\n", mbuf.size);
        printf("frames: %d\n", mbuf.frames);

	for (i=0;i<mbuf.frames;i++) {
	        printf("offset[%d]: %d\n", i, mbuf.offsets[i]);
	} 	
}

jboolean Java_ibis_mbf_media_LinuxWebcam_initWebcam(JNIEnv *env, jobject this, jstring device, jint width, jint height)
{
	const jbyte *str;
	
	str = (*env)->GetStringUTFChars(env, device, NULL);

     	if (str == NULL) {
       		  return JNI_FALSE; /* OutOfMemoryError already thrown */
     	}
     
printf("Opening webcam %s\n", str);

	if ((video_fd = open(str, O_RDWR)) == -1 ) {
                perror("open");
               (*env)->ReleaseStringUTFChars(env, device, str);
		return JNI_FALSE;
        }

        (*env)->ReleaseStringUTFChars(env, device, str);

printf("CAP\n");

        if (ioctl(video_fd, VIDIOCGCAP, &grab_cap) == -1) {
                perror("VIDIOCGCAP");
		return JNI_FALSE;
        }

printf("CAP\n");

        print_capability();

        memset (&grab_pic, 0, sizeof(struct video_picture));

        if (ioctl(video_fd, VIDIOCGPICT, &grab_pic) == -1) {
                perror("VIDIOCGPICT");
		return JNI_FALSE;
        }

printf("PIC\n");

        print_picture();

        if (ioctl(video_fd, VIDIOCGMBUF, &mbuf) == -1) {
                perror("VIDIOCGMBUF");
		return JNI_FALSE;		
        }

printf("BUF\n");

	print_buffer();

        grab_data = mmap(0, mbuf.size, PROT_READ|PROT_WRITE, MAP_SHARED, video_fd, 0);

        grab_buf.format = VIDEO_PALETTE_YUV420P;
//        grab_buf.format = VIDEO_PALETTE_RGB24;
        grab_buf.frame  = 0;
        grab_buf.width  = width;
        grab_buf.height = height;

	frame_size = width*height + (width*height)/2;

	printf("Setting resolution to %dx%d (framesize %d)\n", width, height, frame_size); 

	// Start grabbing first frame immediately	
	if (ioctl(video_fd, VIDIOCMCAPTURE, &grab_buf) == -1) {
		perror("VIDIOCMCAPTURE");
		return JNI_FALSE;
	} 	

	return JNI_TRUE;
}

jboolean Java_Webcam_nextImageRAW(JNIEnv *env, jobject this, jbyteArray dest) 
{ 
	unsigned char *tmp;
	jboolean copy; 

	int current_frame = next_image;

	// Start grabbing next frame immediately
	next_image = (next_image + 1) % mbuf.frames;
        grab_buf.frame = next_image;

	if (ioctl(video_fd, VIDIOCMCAPTURE, &grab_buf) == -1) {
		perror("VIDIOCMCAPTURE");
		return JNI_FALSE;
	} 	

	grab_buf.frame = current_frame;

	// Wait until current frame is ready
	if (ioctl(video_fd, VIDIOCSYNC, &grab_buf) == -1) {
        	perror("VIDIOCSYNC");
		exit(1);
		return JNI_FALSE;
	} 

	// Copy the current frame 
	tmp = (unsigned char *) (*env)->GetPrimitiveArrayCritical(env, dest, &copy);
	
	if (tmp == NULL) { 
		printf("Failed to get ptr to java array!\n");
		return JNI_FALSE;
	} 

	if (copy == JNI_TRUE) { 
		printf("Forced to make copy :-(\n");
	} 

	memcpy(tmp, grab_data+mbuf.offsets[current_frame], grab_buf.width*grab_buf.height+(grab_buf.width*grab_buf.height)/2);
	
	(*env)->ReleasePrimitiveArrayCritical(env, dest, tmp, 0);
	
	return JNI_TRUE;
} 

jboolean Java_ibis_mbf_media_LinuxWebcam_nextImageRGB32(JNIEnv *env, jobject this, jintArray dest) 
{         
	unsigned char *tmp;
	unsigned char *src;
        int count;
        int count2;
     
	jboolean copy; 

	int current_frame = next_image;

	// Start grabbing next frame immediately
	next_image = (next_image + 1) % mbuf.frames;
        grab_buf.frame = next_image;

	if (ioctl(video_fd, VIDIOCMCAPTURE, &grab_buf) == -1) {
		perror("VIDIOCMCAPTURE");
		return JNI_FALSE;
	} 	

	grab_buf.frame = current_frame;

	// Wait until current frame is ready
	if (ioctl(video_fd, VIDIOCSYNC, &grab_buf) == -1) {
        	perror("VIDIOCSYNC");
		exit(1);
		return JNI_FALSE;
	} 

	// Copy the current frame 
	tmp = (unsigned char *) (*env)->GetPrimitiveArrayCritical(env, dest, &copy);
	
	if (tmp == NULL) { 
		printf("Failed to get ptr to java array!\n");
		return JNI_FALSE;
	} 

	if (copy == JNI_TRUE) { 
		printf("Forced to make copy :-(\n");
	} 

	yuvrgb32(grab_data+mbuf.offsets[current_frame], tmp, grab_buf.width, grab_buf.height);

	(*env)->ReleasePrimitiveArrayCritical(env, dest, tmp, 0);

// printf("Returning buffer of %d x %d\n", grab_buf.width, grab_buf.height);
	
	return JNI_TRUE;
} 

jboolean Java_ibis_mbf_media_LinuxWebcam_nextImageRGB24(JNIEnv *env, jobject this, jbyteArray dest) 
{         
	unsigned char *tmp;
	unsigned char *src;
        int count;
        int count2;
     
	jboolean copy; 

	int current_frame = next_image;

	// Start grabbing next frame immediately
	next_image = (next_image + 1) % mbuf.frames;
        grab_buf.frame = next_image;

	if (ioctl(video_fd, VIDIOCMCAPTURE, &grab_buf) == -1) {
		perror("VIDIOCMCAPTURE");
		return JNI_FALSE;
	} 	

	grab_buf.frame = current_frame;

	// Wait until current frame is ready
	if (ioctl(video_fd, VIDIOCSYNC, &grab_buf) == -1) {
        	perror("VIDIOCSYNC");
		exit(1);
		return JNI_FALSE;
	} 

	// Copy the current frame 
	tmp = (unsigned char *) (*env)->GetPrimitiveArrayCritical(env, dest, &copy);
	
	if (tmp == NULL) { 
		printf("Failed to get ptr to java array!\n");
		return JNI_FALSE;
	} 

	if (copy == JNI_TRUE) { 
		printf("Forced to make copy :-(\n");
	} 

	yuvrgb24_alt(grab_data+mbuf.offsets[current_frame], tmp, grab_buf.width, grab_buf.height);

	(*env)->ReleasePrimitiveArrayCritical(env, dest, tmp, 0);
	
	return JNI_TRUE;
} 

/*




jboolean Java_Webcam_nextImageRGB24(JNIEnv *env, jobject this, jbyteArray dest) 
{ 
	unsigned char *tmp;
	jboolean copy; 

	printf("Native called!\n");

	if (ioctl(video_fd, VIDIOCMCAPTURE, &grab_buf) == -1) {
		perror("VIDIOCMCAPTURE");
		return JNI_FALSE;
	} 	

	if (ioctl(video_fd, VIDIOCSYNC, &grab_buf) == -1) {
        	perror("VIDIOCSYNC");
		return JNI_FALSE;
	} 

	printf("Got frame %p %p\n", grab_data, dest);

	tmp = (unsigned char *) (*env)->GetPrimitiveArrayCritical(env, dest, &copy);
	
	if (tmp == NULL) { 
		printf("Failed to get ptr to java array!\n");
		return JNI_FALSE;
	} 

	if (copy == JNI_TRUE) { 
		printf("Forced to make copy :-(\n");
	} 

	yuvrgb24_alt(grab_data, tmp, grab_buf.width, grab_buf.height);
	
	(*env)->ReleasePrimitiveArrayCritical(env, dest, tmp, 0);

	return JNI_TRUE;
} 
*/
