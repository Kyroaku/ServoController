package pl.spamsoftware.servocontroller;

import android.app.*;
import android.os.*;
import android.hardware.*;
import android.widget.*;
import android.bluetooth.*;
import java.io.*;
import android.content.*;

public class MainActivity extends Activity 
{
	class SensorHandler implements SensorEventListener
	{
		float accelerometer[], magnetometer[], orientation[], rotation[];

		SensorHandler()
		{
			accelerometer = new float[] { 0, 0, 0 };
			magnetometer = new float[] { 0, 0, 0 };
			orientation = new float[] { 0, 0, 0 };
			rotation = new float[9];

			SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
			if(sm.registerListener(
				   this,
				   sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				   sm.SENSOR_DELAY_GAME) == false)
				Toast.makeText(context, "No accelerometer sensor.", Toast.LENGTH_SHORT).show();
			if(sm.registerListener(
				   this,
				   sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				   sm.SENSOR_DELAY_GAME) == false)
				Toast.makeText(context, "No magnetic field sensor", Toast.LENGTH_SHORT).show();
			if(sm.registerListener(
				   this,
				   sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				   sm.SENSOR_DELAY_GAME) == false)
				Toast.makeText(context, "No orientation sensor", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSensorChanged(SensorEvent se)
		{
			switch(se.sensor.getType())
			{
				case Sensor.TYPE_ACCELEROMETER:
					accelerometer[0] = se.values[0];
					accelerometer[1] = se.values[1];
					accelerometer[2] = se.values[2];
					SensorManager.getRotationMatrix(rotation, null, accelerometer, magnetometer);
					SensorManager.getOrientation(rotation, orientation);
					onAccelerometerChange(se);
					//onOrientationChange(se);
					break;

				case Sensor.TYPE_MAGNETIC_FIELD:
					magnetometer[0] = se.values[0];
					magnetometer[1] = se.values[1];
					magnetometer[2] = se.values[2];
					SensorManager.getRotationMatrix(rotation, null, accelerometer, magnetometer);
					SensorManager.getOrientation(rotation, orientation);
					onMagneticFieldChanged(se);
					//onOrientationChange(se);
					break;

				case Sensor.TYPE_ORIENTATION:
					onOrientationChange(se);
					break;
			}
		}

		private void onAccelerometerChange(SensorEvent se)
		{
			Float k = 0.1f;
			Float x = new Float(mViewX.getText().toString());
			x = x*(1f-k)+se.values[0]*k;
			Float y = new Float(mViewY.getText().toString());
			y = y*(1f-k)+se.values[1]*k;
			Float z = new Float(mViewZ.getText().toString());
			z = z*(1f-k)+se.values[2]*k;
			mViewX.setText(String.valueOf(Math.round(x*100.0)/100.0));
			mViewY.setText(String.valueOf(Math.round(y*100.0)/100.0));
			mViewZ.setText(String.valueOf(Math.round(z*100.0)/100.0));
			float pitch = (float)(Math.atan2(y, Math.sqrt(z*z+x*x)) / 2.0 / Math.PI) + 0.5f;
			float roll = (float)(Math.atan2(-x, z) / 2.0 / Math.PI) + 0.5f;
			if(pitch < 0.0) pitch += 1.0;
			if(roll < 0.0) roll += 1.0;
			mPitch = (int)(pitch * 1229);
			mRoll = (int)(roll * 1229);

			mViewPitch.setText(String.valueOf(mPitch));
			mViewRoll.setText(String.valueOf(mRoll));
		}

		private void onMagneticFieldChanged(SensorEvent se)
		{
			Float k = 0.1f;
			Float x = new Float(mViewXM.getText().toString());
			x = x*(1f-k)+se.values[0]*k;
			Float y = new Float(mViewYM.getText().toString());
			y = y*(1f-k)+se.values[1]*k;
			Float z = new Float(mViewZM.getText().toString());
			z = z*(1f-k)+se.values[2]*k;
			mViewXM.setText(String.valueOf(Math.round(x*100.0)/100.0));
			mViewYM.setText(String.valueOf(Math.round(y*100.0)/100.0));
			mViewZM.setText(String.valueOf(Math.round(z*100.0)/100.0));

			//float yaw = (float)(Math.atan2((Math.abs(y)/y)*Math.sqrt(y*y+(30+z)*(30+z)), (Math.abs(x)/x)*Math.sqrt(x*x+(30+z)*(30+z))) / 2.0 / Math.PI) + 0.5f;
			float y2 = y / (float)Math.cos(mPitch/1229.0f);
			float x2 = x / (float)Math.cos(mRoll/1229.0f);
			float yaw = (float)Math.atan2(y2, x2);
			if(yaw < 0.0) yaw += 1.0;
			mYaw = (int)(yaw * 1229);
			mViewYaw.setText(String.valueOf(mYaw));
		}

		private void onOrientationChange(SensorEvent se)
		{
			float pitch = se.values[1] + 180.0f;
			float roll = se.values[2] + 180.0f;
			float yaw = se.values[0] + 180.0f;
			if(pitch > 360.0f) pitch -= 360.0f;
			if(roll > 360.0f) roll -= 360.0f;
			if(yaw > 360.0f) yaw -= 360.0f;
			mPitch2 = (int)(pitch/360.0f * 1229);
			mRoll2 = (int)(roll/360.0f * 1229);
			mYaw2 = (int)(yaw/360.0f * 1229);
			mViewPitch2.setText(String.valueOf(mPitch2));
			mViewRoll2.setText(String.valueOf(mRoll2));
			mViewYaw2.setText(String.valueOf(mYaw2));
		}

		@Override
		public void onAccuracyChanged(Sensor s, int p2)
		{
			// TODO: Implement this method
		}
	}

	Context context;
	SensorHandler mSensor;
	TextView
	mViewX, mViewY, mViewZ,
	mViewPitch, mViewRoll, mViewYaw,
	mViewPitch2, mViewRoll2, mViewYaw2,
	mViewXM, mViewYM, mViewZM;
	int
	mPitch, mRoll, mYaw,
	mPitch2, mRoll2, mYaw2;
	Bluetooth mBluetooth;
	BluetoothSocket mBtSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		context = this;
        setContentView(R.layout.main);

		mViewX = (TextView)findViewById(R.id.x);
		mViewY = (TextView)findViewById(R.id.y);
		mViewZ = (TextView)findViewById(R.id.z);
		mViewXM = (TextView)findViewById(R.id.xm);
		mViewYM = (TextView)findViewById(R.id.ym);
		mViewZM = (TextView)findViewById(R.id.zm);
		mViewPitch = (TextView)findViewById(R.id.pitch);
		mViewRoll = (TextView)findViewById(R.id.roll);
		mViewYaw = (TextView)findViewById(R.id.yaw);
		mViewPitch2 = (TextView)findViewById(R.id.pitch2);
		mViewRoll2 = (TextView)findViewById(R.id.roll2);
		mViewYaw2 = (TextView)findViewById(R.id.yaw2);

		mPitch = mRoll = mYaw = 0;

		mBluetooth = new Bluetooth(this);
		mBtSocket = mBluetooth.Connect("XX");
		if(mBtSocket == null) {
			Toast.makeText(this, "Connecting error.", Toast.LENGTH_SHORT).show();
		}

		mSensor = new SensorHandler();

		if(mBtSocket != null)
			new Thread() { public void run() {
					byte buffer[] = new byte[6];

					while(mBtSocket == null) {}
					buffer[0] = (byte)1;
					DXLsend((byte)0xFE, (byte)0x03, (byte)0x18, buffer, (byte)1);

					while(mBtSocket != null)
					{
						buffer[0] = (byte)(mRoll & 0x000000ff);
						buffer[1] = (byte)((mRoll & 0x0000ff00)>>8);
						DXLsend((byte)0x03, (byte)0x03, (byte)0x1E, buffer, (byte)2);
						try
						{
							this.sleep(6);
						}
						catch (InterruptedException e)
						{}
						buffer[0] = (byte)(mPitch & 0x000000ff);
						buffer[1] = (byte)((mPitch & 0x0000ff00)>>8);
						DXLsend((byte)0x02, (byte)0x03, (byte)0x1E, buffer, (byte)2);
						try
						{
							this.sleep(6);
						}
						catch (InterruptedException e)
						{}
						buffer[0] = (byte)(mRoll & 0x000000ff);
						buffer[1] = (byte)((mRoll & 0x0000ff00)>>8);
						//if(mRoll > 620) buffer[0] = 1;
						//else buffer[0] = 0;
						//buffer[0] = (byte)0xFF;
						/*buffer[0] = 0;
						 buffer[1] = 0;
						 buffer[2] = (byte)255;
						 buffer[3] = 3;*/
						DXLsend((byte)0x01, (byte)0x03, (byte)0x1E, buffer, (byte)2);
						try
						{
							this.sleep(6);
						}
						catch (InterruptedException e)
						{}
					}
				}}.start();
    }

	public void DXLsend(byte id, byte ins, byte memaddr, byte data[], byte datalen)
	{
		byte buffer[] = new byte[7+datalen];
		byte checksum = 0;
		buffer[0] = buffer[1] = (byte)0xFF; //2x start byte
		checksum += (buffer[2] = id);
		checksum += (buffer[3] = (byte)(3+datalen));
		checksum += (buffer[4] = ins);
		checksum += (buffer[5] = memaddr);
		for(int i = 0; i < datalen; i++)
			checksum += (buffer[6+i] = data[i]);
		buffer[6+datalen] = (byte)(~checksum);

		mBluetooth.Send(mBtSocket, buffer);
	}

	@Override
	public void onDestroy()
	{
		try
		{
			if(mBtSocket != null)
				mBtSocket.close();
		}
		catch (IOException e)
		{}
		super.onDestroy();
	}
}
