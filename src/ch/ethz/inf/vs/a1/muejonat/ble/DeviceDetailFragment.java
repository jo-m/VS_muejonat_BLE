package ch.ethz.inf.vs.a1.muejonat.ble;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

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
        mConnectionState = STATE_CONNECTING;
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
				mConnectionState = STATE_CONNECTED;
			} else {
				mConnectionState = STATE_DISCONNECTED;
			}
			updateViews();
		}
		
		@Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            
        }

		@Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
        }

	};
	
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
				
				TextView connStatus = ((TextView)getView().findViewById(R.id.device_detail_connection));
				switch(mConnectionState) {
				case STATE_CONNECTED:
					mActivity.hideProgress();
					connStatus.setText("Connected");
					break;
				case STATE_CONNECTING:
					mActivity.showProgress();
					connStatus.setText("Connecting...");
					break;
				case STATE_DISCONNECTED:
					mActivity.hideProgress();
					connStatus.setText("Disconnected");
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
}
