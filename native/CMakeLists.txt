cmake_minimum_required(VERSION 3.7)
project(ClipboardJNI)
set(CMAKE_C_FLAGS "-x objective-c")
link_libraries("-framework AppKit")

set(CMAKE_BUILD_TYPE Release)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(SOURCE_FILES clip_c_macClipboardNative.h clip_c_macClipboardNative.mm)
add_library(ClipboardJNI SHARED ${SOURCE_FILES})