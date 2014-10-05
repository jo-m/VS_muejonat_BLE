package ch.ethz.inf.vs.a1.muejonat.ble;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment representing a single Device detail screen. This fragment is
 * either contained in a {@link DeviceListActivity} in two-pane mode (on
 * tablets) or a {@link DeviceDetailActivity} on handsets.
 */
public class DeviceDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_DEVICE_ID = "bluetooth device id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private BluetoothDevice mDevice;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DeviceDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDevice = getArguments().getParcelable(ARG_DEVICE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_device_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mDevice != null) {
			((TextView) rootView.findViewById(R.id.device_detail))
					.setText(mDevice.toString());
		}

		return rootView;
	}
}
