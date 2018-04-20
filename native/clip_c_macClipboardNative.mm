//
// Created by Tyler on 2018/04/19.
//
#include "clip_c_macClipboardNative.h"
#import <Foundation/Foundation.h>
#import <Appkit/NSPasteboard.h>
#import <AppKit/NSPasteboardItem.h>

JNIEXPORT void JNICALL Java_clip_c_macClipboardNative_setClipboardFiles
        (JNIEnv *env, jobject thisObj, jobjectArray files) {
    printf("Native Method Called");
    for (int i = 0; i < env->GetArrayLength(files); ++i) {
        printf("%s\n", env->GetStringUTFChars((jstring)env->GetObjectArrayElement(files, (jsize)i), JNI_FALSE));
    }

    @autoreleasepool {
        NSMutableArray *fileList = [[NSMutableArray alloc] initWithCapacity: env->GetArrayLength(files)];
        if (fileList) {
            for (int i = 0; i < env->GetArrayLength(files); i++) {
                [fileList addObject: [NSString stringWithUTF8String: env->GetStringUTFChars((jstring)env->GetObjectArrayElement(files, (jsize)i), JNI_FALSE)]];
            }
        }

        NSPasteboard *pBoard =[NSPasteboard  generalPasteboard];
        [pBoard declareTypes:[NSArray arrayWithObject:NSFilenamesPboardType] owner:nil];
        [pBoard setPropertyList:fileList forType:NSFilenamesPboardType];
    }
}
