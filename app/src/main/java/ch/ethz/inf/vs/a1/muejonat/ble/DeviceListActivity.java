package ch.ethz.inf.vs.a1.muejonat.ble;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a list of Devices. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link DeviceDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link DeviceListFragment} and the item details (if present) is a
 * {@link DeviceDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link DeviceListFragment.Callbacks} interface to listen for item selections.
 */
public class DeviceListActivity extends ProgressbarActivity implements
		DeviceListFragment.Callbacks {
	
	private DeviceListFragment mDeviceListFragment;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private final String LIST_FRAGMENT_TAG = "deviceListFragment";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);
        ActionBar ab = getActionBar();
		ab.setTitle("Search Devices");

		if (findViewById(R.id.device_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((DeviceListFragment) getFragmentManager().findFragmentById(
					R.id.device_list)).setActivateOnItemClick(true);
		}
		FragmentManager fm = getFragmentManager();
		mDeviceListFragment = ((DeviceListFragment)fm.findFragmentByTag(LIST_FRAGMENT_TAG));
		
		mDeviceListFragment.startScanning();
	}

	/**
	 * Callback method from {@link DeviceListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(BluetoothDevice dev) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putParcelable(DeviceDetailFragment.ARG_DEVICE_ID, dev);
			DeviceDetailFragment fragment = new DeviceDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.device_detail_container, fragment).commit();
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, DeviceDetailActivity.class);
			detailIntent.putExtra(DeviceDetailFragment.ARG_DEVICE_ID, dev);
			startActivity(detailIntent);
		}
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
            mDeviceListFragment.startScanning();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DeviceListFragment.REQUEST_ENABLE_BT && resultCode != Activity.RESULT_OK) {
            // User did not enable Bluetooth or an error occurred
            Toast.makeText(this, "User did not allow bluetooth, exiting.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
