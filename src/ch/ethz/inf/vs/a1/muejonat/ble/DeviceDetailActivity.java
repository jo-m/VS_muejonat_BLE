package ch.ethz.inf.vs.a1.muejonat.ble;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a single Device detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link DeviceListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link DeviceDetailFragment}.
 */
public class DeviceDetailActivity extends ProgressbarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putParcelable(DeviceDetailFragment.ARG_DEVICE_ID, getIntent()
					.getParcelableExtra(DeviceDetailFragment.ARG_DEVICE_ID));
			DeviceDetailFragment fragment = new DeviceDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.device_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
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
