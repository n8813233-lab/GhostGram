cmake_minimum_required(VERSION 3.22.1)
project(inu_jni CXX C)

# There are no sources here on purpose. liblsplant.so is lsplant's own target; the C entry points
# the rust engine dlopens come from patches-native/lsplant-c-abi.patch, applied to the submodule.

set(DEBUG_SYMBOLS_PATH ${CMAKE_BINARY_DIR}/symbols)

set(LSPLANT_BUILD_SHARED ON CACHE BOOL "" FORCE)
add_subdirectory(lsplant/lsplant/src/main/jni)
