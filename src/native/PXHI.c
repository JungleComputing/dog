#include <jni.h>
#include "ibis_mbf_server_PXHI.h"
//#include "Ops/Array/PxHorus/PxSystem.h"
//#include "Samples/mmGetWeibulls.h"
//#include "Samples/mmGetLabeling.h"


#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    initPXHIsystem
 * Signature: (IIIIIII)V
 */
JNIEXPORT void JNICALL Java_ibis_mbf_server_PXHI_initPXHIsystem
  (JNIEnv *env, jobject this, jint x, jint y, jint z, jint root, 
jint io, jint runParallel, jint runLazy) 
{
	// empty
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    exitPXHIsystem
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_exitPXHIsystem
  (JNIEnv *env, jobject this) 
{ 

}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    abortPXHIsystem
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ibis_mbf_server_PXHI_abortPXHIsystem
  (JNIEnv *env, jobject this) 
{
	// empty
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    getNrCPUs
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_getNrCPUs
  (JNIEnv *env, jobject this) 
{
	// default
	return 1;
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    getMyCPU
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_getMyCPU
  (JNIEnv *env, jobject this) 
{
	// default
	return 0;
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    doRecognize
 * Signature: (II[B[D)V
 */
JNIEXPORT void JNICALL Java_ibis_mbf_server_PXHI_doRecognize
  (JNIEnv *env, jobject this, jint width, jint height, jbyteArray pixels, jdoubleArray vector) 
{
	// empty
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    informAll
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_informAll
  (JNIEnv *env, jobject this, jint value) 
{
	// default
	return value;
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    getNrInvariants
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_getNrInvariants
  (JNIEnv *env, jobject this) 
{
	// random value
	return 6;
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    getNrReceptiveFields
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ibis_mbf_server_PXHI_getNrReceptiveFields
  (JNIEnv *env, jobject this) 
{ 
	// random value	
	return 37;
}

/*
 * Class:     ibis_mbf_server_PXHI
 * Method:    doTrecLabeling
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_ibis_mbf_server_PXHI_doTrecLabeling
  (JNIEnv *env, jobject this, jint width, jint height, jbyteArray pixels) 
{
 	// emtpy	
}
 
#ifdef __cplusplus
}
#endif
