LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := skbitmap_helper

LOCAL_MODULE_FILENAME := libskbitmap_helper

LOCAL_SRC_FILES := skbitmap_helper/SkBitmapHelper.cpp

LOCAL_LDLIBS += -llog -ljnigraphics
#LOCAL_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)

