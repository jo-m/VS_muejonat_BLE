package ch.ethz.inf.vs.a1.muejonat.ble;

import java.util.UUID;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A fragment representing a single Device detail screen. This fragment is
 * either contained in a {@link DeviceListActivity} in two-pane mode (on
 * tablets) or a {@link DeviceDetailActivity} on handsets.
 */
public class DeviceDetailFragment extends Fragment {

	public static final String ARG_DEVICE_ID = "bluetooth device id";
	private BluetoothDevice mDevice;
	private ProgressbarActivity mActivity;
	
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private CONNECTION_STATUS mConnectionState = CONNECTION_STATUS.DISCONNECTED;
    private SERVICE_STATUS mServicesStatus = SERVICE_STATUS.NONE;
    
    private BluetoothGattService mService;
    private BluetoothGattCharacteristic mCharacteristic;
    
    private float temp_deg_c = 0;
    private float hum_rh_perc = 0;
    
    private final UUID RH_T_UUID = UUID.fromString("0000AA20-0000-1000-8000-00805f9b34fb");
    private final UUID RH_T_CHAR_UUID = UUID.fromString("0000AA21-0000-1000-8000-00805f9b34fb");
    
    private final long CYCLE_SLEEP = 3000;
    
    enum SERVICE_STATUS {
    	NONE, DISCOVERING, FINISHED;
    }

    enum CONNECTION_STATUS {
    	DISCONNECTED, CONNECTING, CONNECTED
    }
    

	public DeviceDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDevice = getArguments().getParcelable(ARG_DEVICE_ID);
		mActivity = (ProgressbarActivity)getActivity();
		
		startBluetooth();
	}
	
	@Override
	public void onViewCreated (View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		updateViews();
	}
	
	private void startBluetooth() {
		mBluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            mActivity.finish();
            return;
        }
        
        mBluetoothGatt = mDevice.connectGatt(mActivity, false, mBluetoothCallback);
        mConnectionState = CONNECTION_STATUS.CONNECTING;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_device_detail,
				container, false);

		if (mDevice != null) {
			((TextView) rootView.findViewById(R.id.device_detail_address))
					.setText(mDevice.toString());
		}

		return rootView;
	}
	
	private BluetoothGattCallback mBluetoothCallback = new BluetoothGattCallback() {
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
			if(newState == BluetoothProfile.STATE_CONNECTED) {
				mConnectionState = CONNECTION_STATUS.CONNECTED;
				mBluetoothGatt.discoverServices();
				mServicesStatus = SERVICE_STATUS.DISCOVERING;
			} else {
				mConnectionState = CONNECTION_STATUS.DISCONNECTED;
				mServicesStatus = SERVICE_STATUS.NONE;
			}
			updateViews();
		}
		
		@Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mServicesStatus = SERVICE_STATUS.FINISHED;
            getServiceAndCharacteristic();
            updateViews();
        }

		@Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
			if(status == BluetoothGatt.GATT_SUCCESS) {
				temp_deg_c = (float)mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) / 100;
				hum_rh_perc = (float)mCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2) / 100;
				updateViews();
			}
        }
	};
	
	private void getServiceAndCharacteristic() {
		mService = mBluetoothGatt.getService(RH_T_UUID);
        if(mService == null) {
        	makeToast("This device does not have a RH&T service.");
        	disconnect();
        	close();
        } else {
        	// set the READ perimission on the characteristic
        	BluetoothGattCharacteristic rht = new BluetoothGattCharacteristic(
        			  RH_T_CHAR_UUID,
        	          BluetoothGattCharacteristic.PROPERTY_READ
        	                 | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
        	          BluetoothGattCharacteristic.PERMISSION_READ);
        	// add the characteristic to the discovered RH&T service
        	mService.addCharacteristic(rht);
        	
        	mCharacteristic = mService.getCharacteristic(RH_T_CHAR_UUID);
        	if(mCharacteristic == null) {
        		makeToast("This device does not have a RH&T characteristic.");
            	disconnect();
            	close();
        	} else {
        		new TemperatureReadClass().execute();
        	}
        }
	}
	
	private TemperatureReadClass mUpdater;
	
	private class TemperatureReadClass extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			while(!isCancelled() && mBluetoothGatt != null) {
				mBluetoothGatt.readCharacteristic(mCharacteristic);
				try {
					Thread.sleep(CYCLE_SLEEP);
				} catch (InterruptedException e) {}
				if(mConnectionState != CONNECTION_STATUS.CONNECTED)
					cancel(false);
			}
			return null;
		}
	}

	private void updateViews() {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(getView() == null)
					return;
				TextView address = ((TextView)getView().findViewById(R.id.device_detail_address));
				address.setText(mDevice.getAddress());
				TextView name = ((TextView)getView().findViewById(R.id.device_detail_name));
				name.setText(mDevice.getName());
				
				TextView temp = (TextView)getView().findViewById(R.id.device_detail_temp);
				temp.setText(temp_deg_c + " ¡C");
				
				TextView rh = (TextView)getView().findViewById(R.id.device_detail_rh);
				rh.setText(hum_rh_perc + " %");
				
				TextView connStatus = (TextView)getView().findViewById(R.id.device_detail_connection);
				switch(mConnectionState) {
				case CONNECTED:
					mActivity.hideProgress();
					connStatus.setText("Connected");
					rh.setVisibility(View.VISIBLE);
					temp.setVisibility(View.VISIBLE);
					break;
				case CONNECTING:
					mActivity.showProgress();
					connStatus.setText("Connecting...");
					rh.setVisibility(View.INVISIBLE);
					temp.setVisibility(View.INVISIBLE);
					break;
				case DISCONNECTED:
					mActivity.hideProgress();
					connStatus.setText("Disconnected");
					rh.setVisibility(View.INVISIBLE);
					temp.setVisibility(View.INVISIBLE);
					break;
				}
				
				TextView serviceStatus = (TextView)getView().findViewById(R.id.device_detail_service);
				switch(mServicesStatus) {
				case NONE:
					serviceStatus.setText("");
					break;
				case DISCOVERING:
					serviceStatus.setText("Searching for services...");
					break;
				case FINISHED:
					String num = "No";
					if(mBluetoothGatt != null && mBluetoothGatt.getServices() != null) {
						num = "" + mBluetoothGatt.getServices().size();
					}
					serviceStatus.setText(num + " Services found.");
					break;
				}
			}
		});
	}
	
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        updateViews();
    }

    public void close() {
    	if(mUpdater != null && mUpdater.isCancelled() == false) {
    		mUpdater.cancel(false);
    	}
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    @Override
    public void onStop() {
    	disconnect();
    	close();
    	super.onDestroy();
    }
    
    private void makeToast(final String text) {
    	mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
			}
    	});
    }
}
