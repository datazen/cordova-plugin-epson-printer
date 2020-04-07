package epsonprint.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.Epos2Exception;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.telephony.IccOpenLogicalChannelResponse;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.Base64;

/**
 * This class echoes a string called from JavaScript.
 */
public class EpsonPrint extends CordovaPlugin {

  private Context mContext = null;
  private ArrayList<HashMap<String, String>> mPrinterList = null;
  private CallbackContext _callbackContext = null;
  String strInterface;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("coolMethod")) {
      String message = args.getString(0);
      this.coolMethod(message, callbackContext);
      return true;
    } else if (action.equals("searchPrinter")) {
      String port = args.getString(0);
      this.startDiscovery(port, callbackContext);
      return true;
    }
    return false;
  }

  private void coolMethod(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      callbackContext.success(message);
    } else {
      callbackContext.error("Expected one non-empty string argument.");
    }
  }


  private void stopDiscovery() {
    try {
      Discovery.stop();

      mBtnStart.setEnabled(true);
      mBtnStop.setEnabled(false);
    } catch (Epos2Exception e) {
      if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
        ShowMsg.showException(e, "stop", mContext);
      }
    }
  }

  private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
    @Override
    public void onDiscovery(final DeviceInfo deviceInfo) {
      runOnUiThread(new Runnable() {
        @Override
        public synchronized void run() {
          HashMap<String, String> item = new HashMap<String, String>();
          item.put("PrinterName", deviceInfo.getDeviceName());
          item.put("Target", deviceInfo.getTarget());
          mPrinterList.add(item);
        }
      });
    }
  };

  private void startDiscovery(String strInterface, CallbackContext callbackContext) {

    final CallbackContext _callbackContext = callbackContext;
    final String _strInterface = strInterface;

    mContext = this;
    mPrinterList = new ArrayList<HashMap<String, String>>();

    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        FilterOption filterOption = null;

        mPrinterList.clear();

        filterOption = new FilterOption();
        filterOption.setPortType(((SpnModelsItem) mSpnPortType.getSelectedItem()).getModelConstant());
        filterOption.setBroadcast(mEdtBroadCast.getText().toString());
        filterOption.setDeviceModel(((SpnModelsItem) mSpnModel.getSelectedItem()).getModelConstant());
        filterOption.setEpsonFilter(((SpnModelsItem) mSpnFilter.getSelectedItem()).getModelConstant());
        filterOption.setDeviceType(((SpnModelsItem) mSpnType.getSelectedItem()).getModelConstant());

        try {
          Discovery.start(mContext, filterOption, mDiscoveryListener);
        } catch (Exception e) {
          _callbackContext.error(e.getMessage());
        } finally {

          Log.d("Discovered ports", result.toString());
          _callbackContext.success(result);
        }
      }

    });
  }

}
