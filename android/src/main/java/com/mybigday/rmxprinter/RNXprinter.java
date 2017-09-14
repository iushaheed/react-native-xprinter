package com.mybigday.rnxprinter;

import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Set;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import net.xprinter.utils.XPrinterDev;
import net.xprinter.utils.XPrinterDev.*;
import net.xprinter.utils.DataForSendToPrinterXp80;

public class RNXprinter extends ReactContextBaseJavaModule {
  private String LOG_TAG = "RNXprinter";
  private ReactApplicationContext context;

  private byte[] mBuffer = new byte[0];

  // Bluetooth
  private Set<BluetoothDevice> mPairedDevices;
  private XPrinterDev mBluetoothPrinter = null;

  public RNXprinter(ReactApplicationContext reactContext) {
    super(reactContext);

    this.context = reactContext;

    Log.v(LOG_TAG, "RNXprinter alloc");
  }

  @Override
  public String getName() {
    return "RNXprinter";
  }

  @ReactMethod
  public void getDeviceList(Promise promise){
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      promise.reject("-100", "Not bluetooth adapter");
    }
    else if (bluetoothAdapter.isEnabled()) {
      mPairedDevices = bluetoothAdapter.getBondedDevices();
      WritableArray pairedDeviceList = Arguments.createArray();
      for (BluetoothDevice device : mPairedDevices) {
        WritableMap deviceMap = Arguments.createMap();
        deviceMap.putString("name", device.getName());
        deviceMap.putString("address", device.getAddress());
        pairedDeviceList.pushMap(deviceMap);
      }
      promise.resolve(pairedDeviceList);
    }
    else {
      promise.reject("-103", "BluetoothAdapter not open...");
    }
  }

  @ReactMethod
  public void selectDevice(String address, Promise promise){
    for (BluetoothDevice device : mPairedDevices) {
      Log.d(LOG_TAG, "Checking:" + device.getAddress() + " : " + address);
      if(device.getAddress().equals(address)){
        mBluetoothPrinter = new XPrinterDev(PortType.Bluetooth, address);
        promise.resolve(true);
        return;
      }
    }
    promise.reject("-105", "Device address not exist.");
  }

  @ReactMethod
  public void pushText(String text, Integer size){
    if(size < 0 || size > 7) {
      size = 0;
    }
    Log.d(LOG_TAG, "Set Font Size:" + size);
    pushByteToBuffer(DataForSendToPrinterXp80.selectCharacterSize(size * 17));
    Log.d(LOG_TAG, "Print String:" + text);
    pushByteToBuffer(DataForSendToPrinterXp80.strTobytes(text + "\n"));
  }

  @ReactMethod
  public void printImg(){
  byte[] img = new byte[]{
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x3C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x3F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7E, 0x00, 0x0F, 0x80, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0xFE, 0x00, 0x1F, 0xE0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFE, 0x00,
0x3F, 0xFE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0xFE, 0x00, 0x7F, 0xFF, 0xC0, 0x00, 0x00, 0x00,
0x00, 0x01, 0xFC, 0x01, 0xFF, 0xFF, 0xE0, 0x00, 0x00, 0x00, 0x00, 0x03, 0xF8, 0x03, 0xFF, 0xFF,
0xC0, 0x00, 0x00, 0x00, 0x00, 0x03, 0xF8, 0x07, 0xFF, 0xFF, 0x20, 0x00, 0x00, 0x00, 0x00, 0x03,
0xF0, 0x0F, 0xFF, 0xFE, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x07, 0xF0, 0x3F, 0xFF, 0xFD, 0xA0, 0x00,
0x00, 0x00, 0x00, 0x07, 0xF0, 0x7F, 0xFF, 0xFA, 0x60, 0x00, 0x00, 0x00, 0x00, 0x0F, 0xF0, 0xFD,
0xFF, 0xED, 0xB0, 0x00, 0x00, 0x00, 0x00, 0x3F, 0xE1, 0xC0, 0x3F, 0xDB, 0xF0, 0x00, 0x00, 0x00,
0x00, 0x7F, 0xE7, 0x80, 0x1F, 0xB6, 0xF0, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0x80, 0x1F, 0xEB,
0xE0, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xDF, 0x80, 0x1D, 0x9F, 0x80, 0x00, 0x00, 0x00, 0x01, 0xFF,
0xFF, 0xE0, 0x7B, 0x60, 0x07, 0x80, 0x00, 0x00, 0x01, 0xFF, 0x7F, 0xFF, 0xF4, 0xE0, 0xFF, 0xC0,
0x00, 0x00, 0x01, 0xFD, 0xFF, 0xFF, 0xE9, 0x9F, 0xFF, 0xC0, 0x00, 0x00, 0x03, 0xFF, 0xFF, 0xFF,
0xBB, 0x7F, 0xFF, 0xC0, 0x00, 0x00, 0x0F, 0xF7, 0xFF, 0xFF, 0x65, 0xFF, 0xFF, 0x00, 0x00, 0x00,
0x1F, 0xFF, 0xFF, 0xFE, 0xDF, 0xFF, 0xF0, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFB, 0x37, 0xFF,
0x80, 0x00, 0x00, 0x03, 0xFF, 0x7F, 0xFF, 0xF6, 0x5F, 0xF0, 0x00, 0x00, 0x00, 0x0F, 0xFF, 0x8F,
0xFF, 0xEC, 0xBF, 0x80, 0x00, 0x00, 0x00, 0x3F, 0xFF, 0xB1, 0xFF, 0xDB, 0xFC, 0x00, 0x3F, 0x00,
0x00, 0xFF, 0xFF, 0x3E, 0x3F, 0xA7, 0xF1, 0xBF, 0xFF, 0x80, 0x01, 0xFF, 0xFF, 0xA7, 0xC6, 0xDD,
0xFF, 0xFF, 0xFF, 0x00, 0x03, 0xFF, 0xFF, 0xBC, 0xF9, 0xB7, 0xFF, 0xFF, 0xFE, 0x00, 0x03, 0xFF,
0xFF, 0x07, 0x9F, 0x6F, 0xFF, 0xFF, 0xE0, 0x00, 0x03, 0xFF, 0xFF, 0xE1, 0x70, 0xDF, 0xFF, 0xF8,
0x00, 0x00, 0x03, 0xFF, 0xFF, 0xFC, 0x1F, 0xBF, 0xF8, 0x00, 0x00, 0x00, 0x03, 0xFF, 0xFF, 0xFF,
0x86, 0xFF, 0xE0, 0x00, 0x00, 0x00, 0x03, 0xFF, 0xFF, 0xFF, 0xF1, 0xFF, 0xFF, 0xDC, 0x00, 0x00,
0x03, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x01, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
0xFF, 0xFF, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xF8, 0xFF, 0xFF, 0xFF, 0xFE, 0x00, 0x00, 0x00, 0xFF,
0xFF, 0x80, 0x00, 0x08, 0x1A, 0xAC, 0x00, 0x00, 0x00, 0x7F, 0xFE, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x1F, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0F, 0xF0, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
0x00, 0x00, 0x00, 0x00, 
};
    pushByteToBuffer(img);
    
  }

  @ReactMethod
  public void pushFlashImage(Integer index){
    Log.d(LOG_TAG, "Print FLASH Image:" + index);
    pushByteToBuffer(DataForSendToPrinterXp80.printBmpInFLASH(index, 0));
  }

  @ReactMethod
  public void pushCutPaper(){
    Log.d(LOG_TAG, "Cut Paper");
    pushByteToBuffer(DataForSendToPrinterXp80.selectCutPagerModerAndCutPager(66, 0));
  }

  @ReactMethod
  public void print(Promise promise){
    if(mBluetoothPrinter == null){
      promise.reject("-107", "Must select printer first.");
      return;
    }
    if(mBuffer.length == 0){
      promise.reject("-109", "Buffer is empty");
      return;
    }
    ReturnMessage returnMessage = mBluetoothPrinter.Open();
    Log.d(LOG_TAG, "Open device:" + returnMessage.GetErrorStrings());
    returnMessage = mBluetoothPrinter.Write(mBuffer);
    Log.d(LOG_TAG, "Write data:" + returnMessage.GetErrorStrings());
    clearBuffer();
    returnMessage = mBluetoothPrinter.Close();
    Log.d(LOG_TAG, "Close device:" + returnMessage.GetErrorStrings());
    promise.resolve(true);
  }

  @ReactMethod
  public void clearPrintBuffer(){
    clearBuffer();
  }

  @ReactMethod
  public void printDemoPage(Promise promise){
    clearBuffer();
    pushFlashImage(1);
    pushText("Xprinter TEST\n", 2);
    pushText("如果您看到這個列印結果，表示您離成功非常的近了！加油！！！\n\n", 1);
    pushText("Powered by FuGood MyBigDay Team", 0);
    pushCutPaper();
    print(promise);
  }

  private void pushByteToBuffer(byte[] input){
    byte[] newByte = new byte[mBuffer.length + input.length];
    System.arraycopy(mBuffer, 0, newByte, 0, mBuffer.length);
    System.arraycopy(input, 0, newByte, mBuffer.length, input.length);
    mBuffer = newByte;
    Log.d(LOG_TAG, "Push buffer:" + mBuffer.length);
  }
  private void clearBuffer(){
    mBuffer = new byte[0];
    Log.d(LOG_TAG, "Clear buffer:" + mBuffer.length);
  }
}