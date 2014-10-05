package ch.ethz.inf.vs.a1.muejonat.ble;




import java.util.ArrayList;

import com.android.mail.ui.ButteryProgressBar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private ButteryProgressBar mProgress;
	private boolean mScanning;
    private Handler mHandler;
    
    private static final long SCAN_PERIOD_MS = 10 * 1000;
	
	// Intent request codes
    private static final int REQUEST_ENABLE_BT = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar ab = getActionBar();
		ab.setTitle("BLE");
		ab.setSubtitle("Verteilte Systeme");
		setupProgressBar();
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
		    finish();
		}
		
		final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        mHandler = new Handler();
	}

	@Override
    protected void onPause() {
        super.onPause();
        stopScanning();
        mLeDeviceListAdapter.clear();
    }
	
	@Override
    public void onResume() {
		super.onResume();
		
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        startScanning();
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        
        if (device == null) {
        	Toast.makeText(this, "Error: Device is null!", Toast.LENGTH_SHORT).show();
        	return;
        }
        stopScanning();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			break;
		case R.id.action_scan:
			startScanning();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode != Activity.RESULT_OK) {
			// User did not enable Bluetooth or an error occurred
			Toast.makeText(this, "User did not allow bluetooth, exiting.", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	private synchronized void stopScanning() {
		if(mScanning == false)
			return;
		
		mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
		
        mProgress.setVisibility(View.INVISIBLE);
	}
	
	private synchronized void startScanning() {
		if(mScanning == true)
			return;
		
		mScanning = true;
		mBluetoothAdapter.startLeScan(mLeScanCallback);
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopScanning();
			}
		}, SCAN_PERIOD_MS);

		mProgress.setVisibility(View.VISIBLE);
    }
	
	private LeDeviceListAdapter mLeDeviceListAdapter;

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
	        new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi,
	            byte[] scanRecord) {
	        runOnUiThread(new Runnable() {
	           @Override
	           public void run() {
	               mLeDeviceListAdapter.addDevice(device);
	               mLeDeviceListAdapter.notifyDataSetChanged();
	           }
	       });
	   }
	};

	// Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("InflateParams")
		@Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown device!");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
    
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    
    private void setupProgressBar() {
    	// create new ProgressBar and style it
    	mProgress = new ButteryProgressBar(this);
    	mProgress.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
    	mProgress.setVisibility(View.INVISIBLE);

    	// retrieve the top view of our application
    	final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
    	decorView.addView(mProgress);

    	// Here we try to position the ProgressBar to the correct position by looking
    	// at the position where content area starts. But during creating time, sizes 
    	// of the components are not set yet, so we have to wait until the components
    	// has been laid out
    	// Also note that doing progressBar.setY(136) will not work, because of different
    	// screen densities and different sizes of actionBar
    	ViewTreeObserver observer = mProgress.getViewTreeObserver();
    	observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
    	    @SuppressWarnings("deprecation")
			@Override
    	    public void onGlobalLayout() {
    	        View contentView = decorView.findViewById(android.R.id.content);
    	        mProgress.setY(contentView.getY() - 10);

    	        ViewTreeObserver observer = mProgress.getViewTreeObserver();
    	        observer.removeGlobalOnLayoutListener(this);
    	    }
    	});
    }
}
