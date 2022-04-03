// IOrientationService.aidl
package com.example.aidlserver;

// Declare any non-default types here with import statements

interface IOrientationService {
    /**Get pitch and roll information*/
    int[] getOrientation();
}