/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class mwgrid_middleware_kernel_pdesmas_PDESMASInterface */

#ifndef _Included_mwgrid_middleware_kernel_pdesmas_PDESMASInterface
#define _Included_mwgrid_middleware_kernel_pdesmas_PDESMASInterface
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    construct
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_construct
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    initialise
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_initialise
  (JNIEnv *, jobject, jstring);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    finalise
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_finalise
  (JNIEnv *, jobject);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    getRank
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_getRank
  (JNIEnv *, jobject);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    getSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_getSize
  (JNIEnv *, jobject);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    getGVT
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_getGVT
  (JNIEnv *, jobject);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    sendGVTMessage
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_sendGVTMessage
  (JNIEnv *, jobject);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    add
 * Signature: (JJILmwgrid/middleware/distributedobject/Value;I)Z
 */
JNIEXPORT jboolean JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_add
  (JNIEnv *, jobject, jlong, jlong, jint, jobject, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    read
 * Signature: (JJII)Lmwgrid/middleware/distributedobject/Value;
 */
JNIEXPORT jobject JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_read
  (JNIEnv *, jobject, jlong, jlong, jint, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    write
 * Signature: (JJIII)Z
 */
JNIEXPORT jboolean JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_write__JJIII
  (JNIEnv *, jobject, jlong, jlong, jint, jint, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    write
 * Signature: (JJIDI)Z
 */
JNIEXPORT jboolean JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_write__JJIDI
  (JNIEnv *, jobject, jlong, jlong, jint, jdouble, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    write
 * Signature: (JJILmwgrid/middleware/distributedobject/Location;I)Z
 */
JNIEXPORT jboolean JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_write__JJILmwgrid_middleware_distributedobject_Location_2I
  (JNIEnv *, jobject, jlong, jlong, jint, jobject, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    write
 * Signature: (JJILjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_write__JJILjava_lang_String_2I
  (JNIEnv *, jobject, jlong, jlong, jint, jstring, jint);

/*
 * Class:     mwgrid_middleware_kernel_pdesmas_PDESMASInterface
 * Method:    rangeQuery
 * Signature: (JILmwgrid/middleware/distributedobject/Location;Lmwgrid/middleware/distributedobject/Location;)Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_mwgrid_middleware_kernel_pdesmas_PDESMASInterface_rangeQuery
  (JNIEnv *, jobject, jlong, jint, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif